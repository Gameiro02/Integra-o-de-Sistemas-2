package com.is3.util;

import java.time.LocalDateTime;
import java.util.Random;

import com.is3.model.Sale;
import com.is3.model.SockType;

public class RandomSaleGenerator {

    private static final Random random = new Random();

    public static Sale generateRandomSale() {
        Sale sale = new Sale();
        sale.setSale_id(1);
        sale.setSock_id(Configuration.getRandomSockId());
        sale.setBuyer_id(random.nextInt(500));
        sale.setQuantity_sold(random.nextInt(10) + 1);
        sale.setPricePerPair(Math.round(random.nextDouble() * 10000.0) / 100.0);
        sale.setSale_date(LocalDateTime.now());
        sale.setSock_type(SockType.values()[random.nextInt(SockType.values().length)]);

        return sale;
    }
}
