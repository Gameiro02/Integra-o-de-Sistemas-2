package com.is3.PurchaseOrders;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.is3.model.Purchase;

import java.util.Properties;

public class KafkaPurchaseProducer {
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaPurchaseProducer(String topic) {
        this.topic = topic;
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker1:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(props);
    }

    public void sendPurchase(Purchase purchase) {
        String id = String.valueOf(purchase.getPurchase_id());
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, id, purchase.toJson());
        producer.send(record);
        System.out.println("Sent message: " + record);
    }

    public void close() {
        producer.close();
    }
}
