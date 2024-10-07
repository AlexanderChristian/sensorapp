package com.example.sensorapp.Domain.Consumers.Util;

import java.time.Instant;

public class TimestampedAccelerationAvg {
    private final double x;
    private final double y;
    private final double z;
    private final Instant timestamp;

    public TimestampedAccelerationAvg(double x, double y, double z, Instant timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
