package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.sensorapp.Domain.Constants.ACCELEROMETER;

public class AccelerometerDataProcessor implements DataProcessingFunction {
    private final Map<String, LinkedList<TimestampedAcceleration>> accelerationWindows = new ConcurrentHashMap<>();
    private final long windowDurationMs;

    public AccelerometerDataProcessor(long windowDurationMilliseconds){
        this.windowDurationMs = windowDurationMilliseconds;
    }
    @Override
    public void process(SensorMessage message) {
        if (!ACCELEROMETER.equals(message.getDataType())) {
            return;
        }

        double acceleration = computeAcceleration(message.getData());
        accelerationWindows.computeIfAbsent(message.getSensorId(), k -> new LinkedList<>()).addLast(new TimestampedAcceleration(acceleration,message.getCreatedTime()));

        //Delete older entries
        Instant startOfTimeWindow = message.getCreatedTime().minusMillis(windowDurationMs);
        LinkedList<TimestampedAcceleration> window = accelerationWindows.get(message.getSensorId());
        window.removeIf(timestampedAcceleration -> timestampedAcceleration.timestamp.isBefore(startOfTimeWindow));
    }

    public double computeAcceleration(Object[] data) {
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
