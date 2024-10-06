package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.SensorMessage;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.sensorapp.Domain.Constants.ACCELEROMETER;

public class AccelerometerDataProcessor implements DataProcessingFunction {
    private final Map<String, LinkedList<TimestampedAcceleration>> accelerationWindows = new ConcurrentHashMap<>();

    @Override
    public void process(SensorMessage message) {
        if (!ACCELEROMETER.equals(message.getDataType())) {
            return;
        }

        double acceleration = computeAcceleration(message.getData());
        accelerationWindows.computeIfAbsent(message.getSensorId(), k -> new LinkedList<>()).addLast(new TimestampedAcceleration(acceleration,message.getCreatedTime()));

        //Delete older entries
        Instant oneMinuteAgo = Instant.now(Clock.systemUTC()).minusSeconds(1);
        LinkedList<TimestampedAcceleration> window = accelerationWindows.get(message.getSensorId());
        window.removeIf(timestampedAcceleration -> timestampedAcceleration.timestamp.isBefore(oneMinuteAgo));
    }

    private double computeAcceleration(Object[] data) {
        double x = (double) data[0];
        double y = (double) data[1];
        double z = (double) data[2];
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public double getAverageAcceleration(String sensorId) {
        LinkedList<TimestampedAcceleration> window = accelerationWindows.get(sensorId);
        if (window == null || window.isEmpty()) {
            return 0.0;
        }
        System.out.println(sensorId+" : "+window);
        return window.stream().map(timestampedAcceleration -> timestampedAcceleration.acceleration). mapToDouble(Double::doubleValue).average().orElse(0.0);
    }


}
