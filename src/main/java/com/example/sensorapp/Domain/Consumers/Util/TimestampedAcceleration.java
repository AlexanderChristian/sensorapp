package com.example.sensorapp.Domain.Consumers.Util;

import java.time.Instant;
import java.util.Objects;

public class TimestampedAcceleration {
    private double acceleration;
    private Instant timestamp;

    public TimestampedAcceleration(double acceleration, Instant timestamp) {
        this.acceleration = acceleration;
        this.timestamp = timestamp;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimestampedAcceleration that = (TimestampedAcceleration) o;
        return Double.compare(acceleration, that.acceleration) == 0 && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acceleration, timestamp);
    }
}

