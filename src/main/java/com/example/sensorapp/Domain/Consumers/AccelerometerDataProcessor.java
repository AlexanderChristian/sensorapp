package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.example.sensorapp.Domain.Constants.ACCELEROMETER;

public class AccelerometerDataProcessor implements DataProcessor {
    private final Map<String, ConcurrentLinkedDeque<TimestampedAcceleration>> accelerationWindows = new ConcurrentHashMap<>();
    private final long windowDurationMs;

    public AccelerometerDataProcessor(long windowDurationMilliseconds) {
        this.windowDurationMs = windowDurationMilliseconds;
    }

    @Override
    public void process(SensorMessage message) {
        if (!ACCELEROMETER.equals(message.getDataType())) {
            return;
        }

        double acceleration = computeAcceleration(message.getData());
        accelerationWindows.computeIfAbsent(message.getSensorId(), k -> new ConcurrentLinkedDeque<>()).addLast(new TimestampedAcceleration(acceleration, message.getCreatedTime()));

        //Delete older entries
        Instant createdTime = message.getCreatedTime();
        deleteElementsOutsideWindow(message.getSensorId(), createdTime);
    }


    //I found that a major downside of this approach is the fact that once messages stop coming, you wont be able to delete what remains inside
    //A fallback is needed maybe leveraging Instant now.
    private void deleteElementsOutsideWindow(String sensorId, Instant createdTime) {
        Instant startOfTimeWindow = createdTime.minusMillis(windowDurationMs);

        System.out.println("Deleting elements older than: " + startOfTimeWindow);
        ConcurrentLinkedDeque<TimestampedAcceleration> window = accelerationWindows.get(sensorId);

        Instant fallbackStartOfTimeWindow = Instant.now().minusMillis(windowDurationMs);

        // If the message timestamp is before the fallback window which uses instant as reference, clear the entire window
        if (createdTime.isBefore(fallbackStartOfTimeWindow)) {
            System.out.println("Message too old, clearing entire window for sensor: " + sensorId);
            window.clear();
        }

        window.removeIf(timestampedAcceleration -> {
            boolean shouldRemove = timestampedAcceleration.getTimestamp().isBefore(startOfTimeWindow);
            if (shouldRemove) {
                System.out.println("Removing: " + timestampedAcceleration.getTimestamp());
            }
            return shouldRemove;
        });
    }

    public double computeAcceleration(Object[] data) {
        double x = (double) data[0];
        double y = (double) data[1];
        double z = (double) data[2];
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public double getAverageAcceleration(String sensorId) {
        ConcurrentLinkedDeque<TimestampedAcceleration> window = accelerationWindows.get(sensorId);
        if (window == null || window.isEmpty()) {
            return 0.0;
        }
        deleteElementsOutsideWindow(sensorId, window.getLast().getTimestamp());
        return window.stream().map(timestampedAcceleration -> timestampedAcceleration.getAcceleration())
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }


}
