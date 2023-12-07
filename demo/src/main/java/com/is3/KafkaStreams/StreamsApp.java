package com.is3.KafkaStreams;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.WindowStore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.is3.model.Purchase;
import com.is3.model.Sale;
import com.is3.util.AveragePair;
import com.is3.util.AveragePairSerde;
import com.is3.util.DataBaseSender;
import com.is3.util.LocalDateTimeAdapter;
import com.is3.util.ProfitableSockType;
import com.is3.util.ProfitableSockTypeSerde;

public class StreamsApp {
    private final Gson gson;
    private DataBaseSender dataBaseSender;

    private static final String SALES_TOPIC = "SockSalesTopic";
    private static final String PURCHASES_TOPIC = "SockPurchasesTopic";
    private static final String RESULTS_TOPIC = "ResultsTopic";

    private static final String APP_ID = "sock-shop-streams";
    private static final String BOOTSTRAP_SERVERS1 = "localhost:9092,localhost:9093,localhost:9094";

    public StreamsApp() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        dataBaseSender = new DataBaseSender();

    }

    public static void main(String[] args) {
        StreamsApp app = new StreamsApp();
        app.startStream();
    }

    private Properties setupProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS1);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return props;
    }

    public void startStream() {
        Properties props = setupProperties();

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sourceSales = builder.stream(SALES_TOPIC);
        KStream<String, String> sourcePurchases = builder.stream(PURCHASES_TOPIC);

        revenuePerSockPairSale(sourceSales);
        expensesPerSockPairSale(sourcePurchases);
        profitPerSock(sourceSales, sourcePurchases);

        calculateTotalRevenue(sourceSales);
        calculateTotalExpenses(sourcePurchases);
        calculateTotalProfit(sourceSales, sourcePurchases);

        calculateAveragePurchaseAmountType(sourcePurchases);
        calculateAveragePurchaseAmount(sourcePurchases);

        calculateHighestProfitSockType(sourceSales, sourcePurchases);

        configureRevenueCalculationLastHour(sourceSales);
        configureExpensesCalculationLastHour(sourcePurchases);
        calculateTotalProfitLastHour(sourceSales, sourcePurchases);

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down streams");
            streams.close();
        }));
    }

    /*
     * Todo: Ver 13 e 14
     * Todo: Meter threads para ver se nao fica tao lento
     */

    private Sale deserializeSale(String value) {
        try {
            Sale sales = gson.fromJson(value, Sale.class);
            return sales;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null; // Retorna JSON vazio em caso de erro
        }
    }

    private Purchase deserializePurchase(String value) {
        try {
            Purchase purchase = gson.fromJson(value, Purchase.class);
            return purchase;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null; // Retorna JSON vazio em caso de erro
        }
    }

    /* Req 5 - Get the revenue per sock pair sale */
    private void revenuePerSockPairSale(KStream<String, String> source) {
        source
                .mapValues(this::deserializeSale) // Deserializa para o objeto Sale
                .mapValues(sale -> {
                    Map<String, String> revenueData = new HashMap<>();
                    revenueData.put("revenue", String.format("%.2f", sale.getSale_price() * sale.getQuantity_sold()));

                    return gson.toJson(revenueData);
                })
                .peek((key, value) -> System.out.println("[REVENUE] SockID: " + key + ", " + value))
                .to(RESULTS_TOPIC);
    }

    /* Req 6 - Get the expenses per sock pair sale */
    private void expensesPerSockPairSale(KStream<String, String> source) {
        Gson gson = new GsonBuilder().create();

        source
                .mapValues(this::deserializePurchase) // Deserializa para o objeto Purchase
                .mapValues(purchase -> {
                    Map<String, String> expenseData = new HashMap<>();
                    expenseData.put("result", String.format("%.2f", purchase.getPrice() * purchase.getQuantity()));

                    return gson.toJson(expenseData);
                })
                .peek((key, value) -> System.out.println("[EXPENSE] SockID: " + key + ", " + value))
                .to(RESULTS_TOPIC);
    }

    /* Req 7 - Get the profit per sock pair sale */
    private void profitPerSock(KStream<String, String> revenueStream, KStream<String, String> expenseStream) {
        Gson gson = new GsonBuilder().create();

        // Joining revenue and expenses streams on the sockID
        KStream<String, String> joinedStream = revenueStream.join(
                expenseStream,
                (revenueValue, expenseValue) -> {
                    JsonObject revenueJson = gson.fromJson(revenueValue, JsonObject.class);
                    JsonObject expenseJson = gson.fromJson(expenseValue, JsonObject.class);

                    // Calculate revenue
                    double revenue = 0.0;
                    if (revenueJson != null && revenueJson.has("sale_price")
                            && !revenueJson.get("sale_price").isJsonNull() &&
                            revenueJson.has("quantity_sold") && !revenueJson.get("quantity_sold").isJsonNull()) {
                        revenue = revenueJson.get("sale_price").getAsDouble()
                                * revenueJson.get("quantity_sold").getAsInt();
                    }

                    // Calculate expense
                    double expense = 0.0;
                    if (expenseJson != null && expenseJson.has("price") && !expenseJson.get("price").isJsonNull() &&
                            expenseJson.has("quantity") && !expenseJson.get("quantity").isJsonNull()) {
                        expense = expenseJson.get("price").getAsDouble() * expenseJson.get("quantity").getAsInt();
                    }

                    double profit = revenue - expense;

                    Map<String, String> profitData = new HashMap<>();
                    profitData.put("result", String.format("%.2f", profit));

                    return gson.toJson(profitData);
                },
                JoinWindows.of(Duration.ofSeconds(30)),
                StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String()));

        joinedStream
                .peek((key, value) -> System.out.println("[PROFIT] SockID: " + key + ", " + value))
                .to(RESULTS_TOPIC);
    }

    /* Req 8 - Get the total revenues */
    private void calculateTotalRevenue(KStream<String, String> salesStream) {

        salesStream
                .mapValues(this::deserializeSale)
                .map((key, sale) -> KeyValue.pair("TotalRevenue", sale.getSale_price() * sale.getQuantity_sold()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum)
                .toStream()
                .mapValues(totalRevenue -> {
                    Map<String, String> revenueMap = new HashMap<>();
                    revenueMap.put("result", String.format("%.2f", totalRevenue));
                    return gson.toJson(revenueMap);
                })
                .peek((key, value) -> System.out.println("[TOTAL REVENUE]: " + value))
                .to(RESULTS_TOPIC);
    }

    /* Req 9 - Get the total expenses */
    private void calculateTotalExpenses(KStream<String, String> purchasesStream) {

        purchasesStream
                .mapValues(this::deserializePurchase)
                .map((key, purchase) -> KeyValue.pair("TotalExpenses", purchase.getPrice() * purchase.getQuantity()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum)
                .toStream()
                .mapValues(totalExpenses -> {
                    Map<String, String> expensesMap = new HashMap<>();
                    expensesMap.put("result", String.format("%.2f", totalExpenses));
                    return gson.toJson(expensesMap);
                })
                .peek((key, value) -> System.out.println("[TOTAL EXPENSES]: " + value))
                .to(RESULTS_TOPIC);
    }

    /* Req 10 - Get the total profit */
    public void calculateTotalProfit(KStream<String, String> salesStream, KStream<String, String> purchaseStream) {
        // Processa a stream de vendas para calcular a receita total
        KTable<String, Double> totalSales = salesStream
                .mapValues(this::deserializeSale)
                .map((key, sale) -> KeyValue.pair("Total", sale.getSale_price() * sale.getQuantity_sold()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum);

        // Processa a stream de compras para calcular o total de despesas
        KTable<String, Double> totalPurchases = purchaseStream
                .mapValues(this::deserializePurchase)
                .map((key, purchase) -> KeyValue.pair("Total", purchase.getPrice() * purchase.getQuantity()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum);

        // Calcula o lucro total
        totalSales.join(totalPurchases, (salesTotal, purchaseTotal) -> salesTotal - purchaseTotal)
                .toStream()
                .mapValues(totalProfit -> {
                    Map<String, String> profitMap = new HashMap<>();
                    profitMap.put("result", String.format("%.2f", totalProfit));
                    return gson.toJson(profitMap);
                })
                .peek((key, value) -> System.out.println("[TOTAL PROFIT]: " + value))
                .to("FinalTopic", Produced.with(Serdes.String(), Serdes.String()));
    }

    /*
     * Req 11 - Get the average amount spent in each purchase (separated by sock
     * type).
     */
    public void calculateAveragePurchaseAmountType(KStream<String, String> purchaseStream) {
        // Calcula o total gasto em cada compra e conta o número de compras de cada tipo
        KStream<String, AveragePair> totalAndCountStream = purchaseStream
                .mapValues(this::deserializePurchase)
                .map((key, purchase) -> KeyValue.pair(String.valueOf(purchase.getSock_type()),
                        AveragePair.from(purchase.getPrice() * purchase.getQuantity(), 1)))
                .groupByKey(Grouped.with(Serdes.String(), new AveragePairSerde()))
                .reduce((acc, x) -> AveragePair.from(acc.getTotal() + x.getTotal(), acc.getCount() + x.getCount()))
                .toStream();

        // Calcula a média do valor gasto
        totalAndCountStream
                .mapValues(value -> value.getTotal() / value.getCount())
                .mapValues(average -> {
                    Map<String, String> averageMap = new HashMap<>();
                    averageMap.put("result", String.format("%.2f", average));
                    return gson.toJson(averageMap);
                })
                .peek((key, value) -> System.out.println("[AVERAGE PURCHASE]: SockType: " + key + ", " + value))
                .to(RESULTS_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }

    /*
     * Req 12 - Get the average amount spent in each purchase (aggregated for all
     * socks).
     */
    public void calculateAveragePurchaseAmount(KStream<String, String> purchaseStream) {
        // Calcula o total gasto em cada compra e conta o número de compras
        KStream<String, AveragePair> totalAndCountStream = purchaseStream
                .mapValues(this::deserializePurchase)
                .map((key, purchase) -> KeyValue.pair("average",
                        AveragePair.from(purchase.getPrice() * purchase.getQuantity(), 1)))
                .groupByKey(Grouped.with(Serdes.String(), new AveragePairSerde()))
                .reduce((acc, x) -> AveragePair.from(acc.getTotal() + x.getTotal(), acc.getCount() + x.getCount()))
                .toStream();

        // Calcula a média do valor gasto
        totalAndCountStream
                .mapValues(value -> value.getTotal() / value.getCount())
                .mapValues(average -> {
                    Map<String, String> averageMap = new HashMap<>();
                    averageMap.put("result", String.format("%.2f", average));
                    return gson.toJson(averageMap);
                })
                .peek((key, value) -> System.out.println("[AVERAGE PURCHASE] " + value))
                .to(RESULTS_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }

    /*
     * Req 13 - Get the sock type with the highest profit of all (only one if there
     * is a
     * tie).
     */

    private void calculateHighestProfitSockType(KStream<String, String> salesStream,
            KStream<String, String> purchaseStream) {
        KTable<String, Double> totalSales = salesStream
                .mapValues(this::deserializeSale)
                .filter((key, sale) -> sale != null)
                .map((KeyValueMapper<String, Sale, KeyValue<String, Double>>) (key, sale) -> {
                    double saleAmount = sale.getSale_price() * sale.getQuantity_sold();
                    return KeyValue.pair(String.valueOf(sale.getSock_type()), saleAmount);
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum)
                .mapValues(total -> {
                    return total;
                });

        KTable<String, Double> totalPurchases = purchaseStream
                .mapValues(this::deserializePurchase)
                .filter((key, purchase) -> purchase != null)
                .map((KeyValueMapper<String, Purchase, KeyValue<String, Double>>) (key, purchase) -> {
                    double purchaseAmount = purchase.getPrice() * purchase.getQuantity();
                    return KeyValue.pair(String.valueOf(purchase.getSock_type()), purchaseAmount);
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum)
                .mapValues(total -> {
                    return total;
                });

        KTable<String, Double> totalProfit = totalSales.join(totalPurchases,
                (salesTotal, purchaseTotal) -> {
                    double profit = salesTotal - purchaseTotal;
                    return profit;
                });

        KTable<String, ProfitableSockType> profitableSockType = totalProfit
                .groupBy((key, value) -> KeyValue.pair("maxProfit", new ProfitableSockType(key, value)),
                        Grouped.with(Serdes.String(), new ProfitableSockTypeSerde()))
                .reduce(
                        (aggValue, newValue) -> {
                            if (newValue.getProfit() > aggValue.getProfit()) {
                                return newValue;
                            } else {
                                return aggValue;
                            }
                        },
                        (aggValue, oldValue) -> aggValue);

        profitableSockType.toStream()
                .foreach((key, value) -> System.out
                        .println("[HIGHEST PROFIT]: SockType: " + value.getSockType() + ", " + value.getProfit()));

        profitableSockType.toStream()
                .map((key, value) -> {
                    Map<String, String> profitableSockTypeMap = new HashMap<>();
                    profitableSockTypeMap.put("result", value.getSockType());
                    return KeyValue.pair(key, gson.toJson(profitableSockTypeMap));
                })
                .to(RESULTS_TOPIC);

    }

    /*
     * Req 14 - Get the total revenue in the last hour (use a tumbling time
     * window).
     * Todo: Ver se se pode usar o duration ou se e para usar a data guardada na
     * mensagem
     */
    public void configureRevenueCalculationLastHour(KStream<String, String> sourceSales) {

        // Definindo a janela deslizante para cobrir a última hora
        Duration windowSize = Duration.ofHours(1);

        // Cria uma janela deslizante
        sourceSales
                .selectKey((key, value) -> "constantKey") // Usando uma chave constante
                .groupByKey()
                .windowedBy(TimeWindows.of(windowSize))
                .aggregate(
                        () -> 0.0,
                        (key, value, aggregate) -> {
                            double revenue = extractSaleRevenue(value);
                            return aggregate + revenue;
                        },
                        Materialized.<String, Double, WindowStore<Bytes, byte[]>>as("revenue-store-hourly")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double()))
                .toStream()
                .map((key, value) -> {
                    // Criando um JSON com a receita total
                    JsonObject json = new JsonObject();
                    json.addProperty("result", value);
                    return new KeyValue<>(key.key(), json.toString());
                })
                .peek((key, value) -> System.out.println("[REVENUE LAST HOUR]: " + value))
                .to(RESULTS_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }

    private Double extractSaleRevenue(String saleJson) {
        Sale sale = gson.fromJson(saleJson, Sale.class);
        double revenue = sale.getSale_price() * sale.getQuantity_sold();
        return revenue;
    }

    /*
     * Req 15 - Get the total expenses in the last hour (use a tumbling time
     * window).
     * Todo: Ver se se pode usar o duration ou se e para usar a data guardada na
     * mensagem
     */

    public void configureExpensesCalculationLastHour(KStream<String, String> sourcePurchases) {

        // Definindo a janela deslizante para cobrir a última hora
        Duration windowSize = Duration.ofHours(1);

        // Cria uma janela deslizante
        sourcePurchases
                .selectKey((key, value) -> "constantKey") // Usando uma chave constante
                .groupByKey()
                .windowedBy(TimeWindows.of(windowSize))
                .aggregate(
                        () -> 0.0,
                        (key, value, aggregate) -> {
                            double expense = extractPurchaseExpense(value);
                            return aggregate + expense;
                        },
                        Materialized.<String, Double, WindowStore<Bytes, byte[]>>as("expense-store-hourly")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double()))
                .toStream()
                .map((key, value) -> {
                    // Criando um JSON com a receita total
                    JsonObject json = new JsonObject();
                    json.addProperty("result", value);
                    return new KeyValue<>(key.key(), json.toString());
                })
                .peek((key, value) -> System.out.println("[EXPENSES LAST HOUR]: " + value))
                .to(RESULTS_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }

    private Double extractPurchaseExpense(String purchaseJson) {
        Purchase purchase = gson.fromJson(purchaseJson, Purchase.class);
        double expense = purchase.getPrice() * purchase.getQuantity();
        return expense;
    }

    /*
     * Req 16 - Get the total profits in the last hour (use a tumbling time window).
     */
    public void calculateTotalProfitLastHour(KStream<String, String> salesStream,
            KStream<String, String> purchaseStream) {
        // Defining the window size to cover the last hour
        Duration windowSize = Duration.ofHours(1);

        // Process the sales stream to calculate the total revenue
        KTable<Windowed<String>, Double> totalSalesLastHour = salesStream
                .mapValues(this::deserializeSale)
                .map((key, sale) -> KeyValue.pair("Total", sale.getSale_price() * sale.getQuantity_sold()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .windowedBy(TimeWindows.of(windowSize))
                .reduce(Double::sum);

        // Process the purchases stream to calculate the total expenses
        KTable<Windowed<String>, Double> totalPurchasesLastHour = purchaseStream
                .mapValues(this::deserializePurchase)
                .map((key, purchase) -> KeyValue.pair("Total", purchase.getPrice() * purchase.getQuantity()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .windowedBy(TimeWindows.of(windowSize))
                .reduce(Double::sum);

        // Calculate the total profit
        totalSalesLastHour.join(totalPurchasesLastHour, (salesTotal, purchaseTotal) -> salesTotal - purchaseTotal)
                .toStream()
                .map((key, totalProfit) -> {
                    Map<String, String> profitMap = new HashMap<>();
                    profitMap.put("total_profit_hour", String.format("%.2f", totalProfit));
                    return new KeyValue<>(key.key(), gson.toJson(profitMap));
                })
                .peek((key, value) -> System.out.println("[PROFIT LAST HOUR]: " + value))
                .to(RESULTS_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }

    /*
     * Req 17 - Get the name of the sock supplier generating the highest profit
     * sales. Include the value of such sales.
     */

}
