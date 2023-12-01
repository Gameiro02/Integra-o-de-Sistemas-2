package com.is3.util;

import java.time.LocalDateTime;
import java.util.Random;

import com.is3.model.Purchase;

public class RandomPurchaseGenerator {

    private static final Random random = new Random();

    public static Purchase generateRandomPurchase() {
        Purchase purchase = new Purchase();
        purchase.setPurchase_id(random.nextInt(1000));
        purchase.setSock_id(Configuration.getRandomSockId());
        purchase.setQuantity(random.nextInt(10) + 1);
        purchase.setPrice(random.nextDouble() * 100);
        purchase.setPurchase_date(LocalDateTime.now());

        return purchase;
    }
}
