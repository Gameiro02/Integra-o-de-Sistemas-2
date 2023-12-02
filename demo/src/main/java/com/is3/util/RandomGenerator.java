package com.is3.util;

import java.time.LocalDateTime;
import java.util.Random;

import com.is3.model.Purchase;
import com.is3.model.Sale;
import com.is3.model.Sock;
import com.is3.model.SockType;

public class RandomGenerator {
    
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

    public static Sale generateRandomSale() {
        Sale sale = new Sale();
        sale.setSale_id(random.nextInt(1000));
        sale.setSock_id(Configuration.getRandomSockId());
        sale.setBuyer_id(random.nextInt(500));
        sale.setQuantity_sold(random.nextInt(10) + 1);
        sale.setPricePerPair(random.nextDouble() * 100);
        sale.setSale_date(LocalDateTime.now());

        return sale;
    }

    public static Sock generateRandomSock() {
        Sock sock = new Sock();
        sock.setSock_id(random.nextInt(1000));
        sock.setType(SockType.values()[random.nextInt(SockType.values().length)]);
        sock.setPrice(random.nextDouble() * 100);
        sock.setQuantity_available(random.nextInt(50) + 1);
        sock.setSupplier_id(random.nextInt(100));

        return sock;
    }
}
