package com.is3.Customer;

import com.is3.model.Sale;
import com.is3.util.RandomSaleGenerator;

public class SalesApp {
    public static void main(String[] args) {
        KafkaSalesProducer producer = new KafkaSalesProducer("SockSalesTopic");

        double totalSales = 0;
        int numberOfSales = 5; // Número de vendas a serem geradas

        for (int i = 0; i < 1; i++) {
            Sale sale = RandomSaleGenerator.generateRandomSale();
            producer.sendSale(sale);

            // Adiciona o valor da venda atual ao total
            totalSales += sale.getSale_price() * sale.getQuantity_sold();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();

        // Calcula a média das vendas
        double averageSales = totalSales / numberOfSales;

        // Imprime o total e a média
        System.out.println("Total Sales (euros): " + totalSales);
        System.out.println("Average Sale Value: " + averageSales);
    }
}
