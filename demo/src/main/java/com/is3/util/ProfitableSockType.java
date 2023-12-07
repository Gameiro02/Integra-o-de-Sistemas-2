package com.is3.util;

public class ProfitableSockType {
    private String sockType;
    private double profit;

    public ProfitableSockType(String sockType, double profit) {
        this.sockType = sockType;
        this.profit = profit;
    }

    public String getSockType() {
        return sockType;
    }

    public void setSockType(String sockType) {
        this.sockType = sockType;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = Math.round(profit * 100.0) / 100.0;
    }
}