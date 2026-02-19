package com.fishtycoon.model;

public class BackpackFishEntry {
    private final String fishId;
    private final double size;
    private int amount;

    public BackpackFishEntry(String fishId, double size, int amount) {
        this.fishId = fishId;
        this.size = size;
        this.amount = amount;
    }

    public String getFishId() {
        return fishId;
    }

    public double getSize() {
        return size;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
