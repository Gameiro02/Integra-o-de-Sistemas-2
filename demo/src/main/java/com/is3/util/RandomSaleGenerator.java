package com.is3.util;

import java.time.LocalDateTime;
import java.util.Random;

import com.is3.model.Sale;

public class RandomSaleGenerator {

    private static final Random random = new Random();

    public static Sale generateRandomSale() {
        Sale sale = new Sale();
        sale.setSale_id(random.nextInt(1000));
        sale.setSock_id(1);
        sale.setBuyer_id(random.nextInt(500));
        sale.setQuantity_sold(random.nextInt(10) + 1);
        sale.setPricePerPair(Math.round(random.nextDouble() * 10000.0) / 100.0);
        sale.setSale_date(LocalDateTime.now());

        return sale;
    }
}
