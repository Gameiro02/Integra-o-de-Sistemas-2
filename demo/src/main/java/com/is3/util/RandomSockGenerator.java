package com.is3.util;

import com.is3.model.Sock;
import com.is3.model.SockType;

import java.util.Random;


public class RandomSockGenerator {
    
    private static final Random random = new Random();

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
