package com.is3.util;

public class Configuration {
    // Array de sock_ids disponíveis
    public static final int[] availableSockIds = { 1, 2, 3, 10, 20, 30, 40, 50 };

    // Método para obter um sock_id aleatório do array disponível
    public static int getRandomSockId() {
        return availableSockIds[(int) (Math.random() * availableSockIds.length)];
    }
}
