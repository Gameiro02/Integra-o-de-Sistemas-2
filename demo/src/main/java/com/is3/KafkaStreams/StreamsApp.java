package com.is3.KafkaStreams;

import java.util.Properties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.is3.model.Sale;
import com.is3.util.LocalDateTimeAdapter;
import com.is3.util.ProfitData;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import java.time.LocalDateTime;

public class StreamsApp {
    private final Gson gson;
    private double totalProfit = 0.0; // Variável para manter o lucro total

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
         * Todo: Ver se se pode usar uma variavel global para o lucro total ????
         */

        source.mapValues(this::processSale)
                .mapValues(profitPerSale -> {
                    totalProfit += profitPerSale; // Atualiza o lucro total

                    ProfitData profitData = new ProfitData(profitPerSale, totalProfit);
                    String jsonProfitData = gson.toJson(profitData);

                    System.out.println("Enviando mensagem: " + jsonProfitData); // Printa a mensagem

                    return jsonProfitData;
                })
                .to("ResultsTopic");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down streams");
            streams.close();
        }));
    }

    private Double processSale(String value) {
        try {
            Sale sale = gson.fromJson(value, Sale.class);
            return calculateProfit(sale);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return 0.0; // Retorna 0 em caso de erro
        }
    }

    private double calculateProfit(Sale sale) {
        return sale.getSale_price() * sale.getQuantity_sold(); // Lucro = sale_price * quantity_sold
    }
}
