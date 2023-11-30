package com.is3.KafkaStreams;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

public class StreamsApp {
    
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, String.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,String.class.getName());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> source = builder.stream("Sales");

        source.peek((key, value) -> System.out.println("Received message. Key: " + key + " Value: " + value))
                .to("Results");

        // // Process the stream as required
        // // For now, just forwarding the messages as they are
        // source.to("Results");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.setStateListener((newState, oldState) ->
            System.out.println("State changed from " + oldState + " to " + newState));

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down streams");
            streams.close();
        }));
    }
}

