package com.is3.KafkaStreams;

import java.util.Properties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.is3.model.Purchase;
import com.is3.model.Sale;
import com.is3.util.ExpenseData;
import com.is3.util.LocalDateTimeAdapter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.time.LocalDateTime;

import com.is3.util.RevenueData;
import com.is3.util.ExpenseData;

public class StreamsApp {
    private final Gson gson;
    private double totalRevenue = 0.0; // Variável para manter o lucro total
    private double totalExpense = 0.0; // Variável para manter o custo total

    public StreamsApp() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
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
        KStream<String, String> source = builder.stream("SockSalesTopic");

        /*
         * Para calcular o "Get the revenue per sock pair sale" faz-se revenue = preco *
         * quantidade e escreve-se para o topico
         * Mais tarde e preciso atualizar na base de dados o valor do lurco de cada par
         * de meias vendido
         * 
         * Todo: Meter para json, perguntar o que meter no json?
         * Todo: Ver se esta bem calculado
         * Todo: Ver se se pode usar uma variavel global para o lucro e para as
         * expenses
         */

        // SockSalesTopic
        KStream<String, String> sourceSales = builder.stream("SockSalesTopic");
        sourceSales
                .mapValues(this::processSale)
                .peek((key, value) -> System.out.println("Enviando mensagem de venda: " + value))
                .to("ResultsTopicSale", Produced.with(Serdes.String(), Serdes.String()));

        // SockPurchasesTopic
        KStream<String, String> sourcePurchases = builder.stream("SockPurchasesTopic");
        sourcePurchases
                .mapValues(this::processPurchase)
                .peek((key, value) -> System.out.println("Enviando mensagem de compra: " + value))
                .to("ResultsTopicPurchase", Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down streams");
            streams.close();
        }));
    }

    private String processSale(String value) {
        try {
            Sale sale = gson.fromJson(value, Sale.class);
            return createRevenueMessage(sale);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return "{}"; // Retorna JSON vazio em caso de erro
        }
    }

    private double calculateProfit(Sale sale) {
        return sale.getSale_price() * sale.getQuantity_sold();
    }

    private String createRevenueMessage(Sale sale) {
        double profitPerSale = calculateProfit(sale);
        totalRevenue += profitPerSale;
        RevenueData profitData = new RevenueData(sale.getSock_id(), profitPerSale, totalRevenue);
        return gson.toJson(profitData);
    }

    private String createExpenseMessage(Purchase purchase) {
        double expensePerPair = calculateCostPerPair(purchase);
        totalExpense += expensePerPair * purchase.getQuantity();
        ExpenseData expenseData = new ExpenseData(purchase.getSock_id(), expensePerPair, totalExpense);
        return gson.toJson(expenseData);
    }

    private String processPurchase(String value) {
        Purchase purchase = gson.fromJson(value, Purchase.class);
        return createExpenseMessage(purchase);
    }

    private double calculateCostPerPair(Purchase purchase) {
        return purchase.getPrice() / purchase.getQuantity();
    }

}
