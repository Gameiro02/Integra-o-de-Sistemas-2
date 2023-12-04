package com.is3.KafkaStreams;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import com.is3.model.Purchase;
import com.is3.model.Sale;
import com.is3.util.LocalDateTimeAdapter;

public class StreamsApp {
    private final Gson gson;
    private final Producer<String, String> producer;
    private double totalRevenue = 0.0; // Variável para manter o lucro total
    private double totalExpense = 0.0; // Variável para manter o custo total

    private int totalPurchaseCount = 0;

    public StreamsApp() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        // Configurações do Produtor
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", "broker1:9092");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(producerProps);
    }

    public static void main(String[] args) {
        StreamsApp app = new StreamsApp();
        app.startStream();
    }

    public void startStream() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sock-shop-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "broker1:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sourceSales = builder.stream("SockSalesTopic");
        KStream<String, String> sourcePurchases = builder.stream("SockPurchasesTopic");

        revenuePerSockPairSale(sourceSales);
        expensesPerSockPairSale(sourcePurchases);

        calculateTotalRevenue(sourceSales);
        calculateTotalExpenses(sourcePurchases);

        calculateTotalProfit(sourceSales, sourcePurchases);

        configureRevenueCalculationLastHour(sourceSales);
        configureExpensesCalculationLastHour(sourcePurchases);

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down streams");
            producer.close();
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
        Gson gson = new GsonBuilder().create();

        source
                .mapValues(this::deserializeSale) // Deserializa para o objeto Sale
                .mapValues(sale -> {
                    Map<String, String> revenueData = new HashMap<>();
                    revenueData.put("saleId", String.valueOf(sale.getSale_id()));
                    revenueData.put("revenue", String.format("%.2f", sale.getSale_price() * sale.getQuantity_sold()));

                    return gson.toJson(revenueData);
                })
                .peek((key, value) -> System.out.println("Enviando para o tópico - Key: " + key + ", Value: " + value))
                .to("ResultsTopicSale");
    }

    /* Req 6 - Get the expenses per sock pair sale */
    private void expensesPerSockPairSale(KStream<String, String> source) {
        Gson gson = new GsonBuilder().create();

        source
                .mapValues(this::deserializePurchase) // Deserializa para o objeto Purchase
                .mapValues(purchase -> {
                    Map<String, String> expenseData = new HashMap<>();
                    expenseData.put("purchaseId", String.valueOf(purchase.getPurchase_id()));
                    expenseData.put("expense", String.format("%.2f", purchase.getPrice() * purchase.getQuantity()));

                    return gson.toJson(expenseData);
                })
                .peek((key, value) -> System.out.println("Enviando para o tópico - Key: " + key + ", Value: " + value))
                .to("ResultsTopicPurchase");
    }

    /* Req 7 - Get the profit per sock pair sale */

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
                    revenueMap.put("total_revenue", String.format("%.2f", totalRevenue));
                    return gson.toJson(revenueMap);
                })
                .peek((key, value) -> System.out.println("Enviando para o tópico - Key: " + key + ", Value: " + value))
                .to("ResultsTopicSale");
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
                    expensesMap.put("total_expenses", String.format("%.2f", totalExpenses));
                    return gson.toJson(expensesMap);
                })
                .peek((key, value) -> System.out.println("Enviando para o tópico - Key: " + key + ", Value: " + value))
                .to("ResultsTopicPurchase");
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
                    profitMap.put("total_profit", String.format("%.2f", totalProfit));
                    return gson.toJson(profitMap);
                })
                .peek((key, value) -> System.out.println("Enviando para o tópico - Key: " + key + ", Value: " + value))
                .to("ResultsTopicSale", Produced.with(Serdes.String(), Serdes.String()));
    }

    /*
     * Req 11 - Get the average amount spent in each purchase (separated by sock
     * type).
     */

    /*
     * Req 12 - Get the average amount spent in each purchase (aggregated for all
     * socks).
     */

    /*
     * Req 13 - Get the sock type with the highest profit of all (only one if there
     * is a
     * tie).
     */

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
                    json.addProperty("totalRevenueHour", value);
                    return new KeyValue<>(key.key(), json.toString());
                })
                .peek((key, value) -> System.out.println("Enviando para o tópico - Key: " + key + ", Value: " + value))
                .to("TotalRevenueLastHourTopic", Produced.with(Serdes.String(), Serdes.String()));
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
                    json.addProperty("totalExpenseHour", value);
                    return new KeyValue<>(key.key(), json.toString());
                })
                .peek((key, value) -> System.out.println("Enviando para o tópico - Key: " + key + ", Value: " + value))
                .to("TotalExpenseLastHourTopic", Produced.with(Serdes.String(), Serdes.String()));
    }

    private Double extractPurchaseExpense(String purchaseJson) {
        Purchase purchase = gson.fromJson(purchaseJson, Purchase.class);
        double expense = purchase.getPrice() * purchase.getQuantity();
        return expense;
    }

    /*
     * Req 16 - Get the total profits in the last hour (use a tumbling time window).
     */

    /*
     * Req 17 - Get the name of the sock supplier generating the highest profit
     * sales. Include the value of such sales.
     */
}
