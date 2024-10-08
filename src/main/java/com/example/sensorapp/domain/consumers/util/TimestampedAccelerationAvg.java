package com.example.sensorapp.domain.consumers.util;

import java.time.Instant;

public record TimestampedAccelerationAvg(double x, double y, double z, Instant timestamp) {
}
