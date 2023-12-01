package com.is3.util;

public class RevenueData {
    private double profitPerSale;
    private double totalRevenue;
    private int sockId;

    public RevenueData(int sockId, double profitPerSale, double totalRevenue) {
        this.profitPerSale = profitPerSale;
        this.totalRevenue = totalRevenue;
        this.sockId = sockId;
    }

    public double getProfitPerSale() {
        return profitPerSale;
    }

    public void setProfitPerSale(double profitPerSale) {
        this.profitPerSale = profitPerSale;
    }

    public double gettotalRevenue() {
        return totalRevenue;
    }

    public void settotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getSockId() {
        return sockId;
    }

    public void setSockId(int sockId) {
        this.sockId = sockId;
    }

}
