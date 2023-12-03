package com.is3.util;

public class ExpenseData {
    private double expensePerPair;
    private double totalExpense;
    private int sockId;
    private double averageExpense;

    public ExpenseData(int sockId, double expensePerPair, double totalExpense, double averageExpense) {
        this.expensePerPair = expensePerPair;
        this.totalExpense = totalExpense;
        this.sockId = sockId;
        this.averageExpense = averageExpense;
    }

    public double getExpensePerPair() {
        return expensePerPair;
    }

    public void setExpensePerPair(double expensePerPair) {
        this.expensePerPair = expensePerPair;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public int getSockId() {
        return sockId;
    }

    public void setSockId(int sockId) {
        this.sockId = sockId;
    }

}
