package com.is3.util;

import java.time.LocalDateTime;
import java.util.Random;

import com.is3.model.Purchase;
import com.is3.model.SockType;

public class RandomPurchaseGenerator {

    private static final Random random = new Random();

    public static Purchase generateRandomPurchase() {
        Purchase purchase = new Purchase();
        purchase.setPurchase_id(1);
        purchase.setSock_id(Configuration.getRandomSockId());
        purchase.setQuantity(random.nextInt(10) + 1);
        purchase.setPrice(Math.round(random.nextDouble() * 10000.0) / 100.0);
        purchase.setPurchase_date(LocalDateTime.now());
        purchase.setSock_type(SockType.values()[random.nextInt(SockType.values().length)]);

        return purchase;
    }
}
