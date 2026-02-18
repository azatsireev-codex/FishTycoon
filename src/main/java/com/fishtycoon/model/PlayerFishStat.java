package com.fishtycoon.model;

public class PlayerFishStat {
    private int timesCaught;
    private double largestSize;
    private long firstCaughtAt;

    public int getTimesCaught() { return timesCaught; }
    public void setTimesCaught(int timesCaught) { this.timesCaught = timesCaught; }
    public double getLargestSize() { return largestSize; }
    public void setLargestSize(double largestSize) { this.largestSize = largestSize; }
    public long getFirstCaughtAt() { return firstCaughtAt; }
    public void setFirstCaughtAt(long firstCaughtAt) { this.firstCaughtAt = firstCaughtAt; }
}
