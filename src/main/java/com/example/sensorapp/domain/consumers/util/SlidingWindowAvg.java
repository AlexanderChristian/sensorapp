package com.example.sensorapp.domain.consumers.util;


import java.time.Instant;

public class SlidingWindowAvg {

    String sensorId;
    private double avgX;
    private double avgY;
    private double avgZ;
    Instant start;
    Instant end;

    public SlidingWindowAvg() {
    }

    public SlidingWindowAvg(String sensorId, double avgX, double avgY, double avgZ, Instant start, Instant end) {
        this.sensorId = sensorId;
        this.avgX = avgX;
        this.avgY = avgY;
        this.avgZ = avgZ;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "SlidingWindowAvg{" +
                "sensorId='" + sensorId + '\'' +
                ", avgX=" + avgX +
                ", avgY=" + avgY +
                ", avgZ=" + avgZ +
                ", start=" + start +
                ", end=" + end +
                '}';
    }

    public String getSensorId() {
        return sensorId;
    }

    public double getAvgX() {
        return avgX;
    }

    public double getAvgY() {
        return avgY;
    }

    public double getAvgZ() {
        return avgZ;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }
}