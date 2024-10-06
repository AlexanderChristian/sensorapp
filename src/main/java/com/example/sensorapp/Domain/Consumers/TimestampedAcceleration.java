package com.example.sensorapp.Domain.Consumers;

import java.time.Instant;

public class TimestampedAcceleration {
    double acceleration;
    Instant timestamp;

    public TimestampedAcceleration(double acceleration, Instant timestamp) {
        this.acceleration = acceleration;
        this.timestamp = timestamp;
    }
}

