package com.is3.util;

public class RevenueData {
    private double revenuePerSale;
    private double totalRevenue;
    private int sockId;

    public RevenueData(int sockId, double revenuePerSale, double totalRevenue) {
        this.revenuePerSale = revenuePerSale;
        this.totalRevenue = totalRevenue;
        this.sockId = sockId;
    }

    public double getrevenuePerSale() {
        return revenuePerSale;
    }

    public void setrevenuePerSale(double revenuePerSale) {
        this.revenuePerSale = revenuePerSale;
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
