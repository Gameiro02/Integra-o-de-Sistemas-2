package com.is3.Customer;

import com.is3.model.Sale;
import com.is3.util.RandomGenerator;

public class SalesApp {
    public static void main(String[] args) {
        KafkaSalesProducer producer = new KafkaSalesProducer("SockSalesTopic");

        for (int i = 0; i < 100; i++) {
            Sale sale = RandomGenerator.generateRandomSale();
            producer.sendSale(sale);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }
}

// kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic
// ResultsTopic --from-beginning
