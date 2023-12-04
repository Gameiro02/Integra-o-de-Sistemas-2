package com.is3.util;

public class AveragePair {
    private double total;
    private long count;

    private AveragePair(double total, long count) {
        this.total = total;
        this.count = count;
    }

    public static AveragePair from(double total, long count) {
        return new AveragePair(total, count);
    }

    public double getTotal() {
        return total;
    }

    public long getCount() {
        return count;
    }

}
