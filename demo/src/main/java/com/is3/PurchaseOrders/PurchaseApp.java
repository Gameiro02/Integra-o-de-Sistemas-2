package com.is3.PurchaseOrders;

import com.is3.model.Purchase;
import com.is3.util.RandomPurchaseGenerator;

public class PurchaseApp {
    public static void main(String[] args) {
        KafkaPurchaseProducer producer = new KafkaPurchaseProducer("SockPurchasesTopic");

        double totalPurchases = 0;
        int numberOfPurchases = 5; // Número de compras a serem geradas

        for (int i = 0; i < 1; i++) {
            Purchase purchase = RandomPurchaseGenerator.generateRandomPurchase();
            producer.sendPurchase(purchase);

            // Adiciona o valor da compra atual ao total
            totalPurchases += purchase.getPrice() * purchase.getQuantity();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();

        // Calcula a média das compras
        double averagePurchase = totalPurchases / numberOfPurchases;

        // Imprime o total e a média
        System.out.println("Total Purchases: " + totalPurchases);
        System.out.println("Average Purchase Value: " + averagePurchase);
    }
}
