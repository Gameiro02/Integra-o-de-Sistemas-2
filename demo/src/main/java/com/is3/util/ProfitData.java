package com.is3.util;

public class ProfitData {
    private double profitPerSale;
    private double totalProfit;
    private int sockId;

    public ProfitData(int sockId, double profitPerSale, double totalProfit) {
        this.profitPerSale = profitPerSale;
        this.totalProfit = totalProfit;
        this.sockId = sockId;
    }

    public double getProfitPerSale() {
        return profitPerSale;
    }

    public void setProfitPerSale(double profitPerSale) {
        this.profitPerSale = profitPerSale;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public int getSockId() {
        return sockId;
    }

    public void setSockId(int sockId) {
        this.sockId = sockId;
    }

}
