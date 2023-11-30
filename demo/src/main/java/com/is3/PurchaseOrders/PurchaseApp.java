package com.is3.PurchaseOrders;

import com.is3.model.Purchase;
import com.is3.util.RandomPurchaseGenerator;

public class PurchaseApp {
    public static void main(String[] args) {
        KafkaPurchaseProducer producer = new KafkaPurchaseProducer("SockPurchasesTopic");

        for (int i = 0; i < 100; i++) {
            Purchase purchase = RandomPurchaseGenerator.generateRandomPurchase();
            producer.sendPurchase(purchase);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }
}
