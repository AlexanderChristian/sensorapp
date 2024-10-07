package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;

import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.example.sensorapp.Domain.Constants.ACCELEROMETER;

public class AccelerometerDataProcessor implements DataProcessor {
    private final ConcurrentLinkedDeque<TimestampedAcceleration> accelerationWindow = new ConcurrentLinkedDeque<>();
    private long windowDurationMs = 60000; // default, maybe make configurable
    private final NormalizationStrategy normalizationStrategy;
    private final String sensorId;

    public AccelerometerDataProcessor(String sensorId, NormalizationStrategy strategy) {
        this.sensorId = sensorId;
        this.normalizationStrategy = strategy;
    }

    public AccelerometerDataProcessor(String sensorId, NormalizationStrategy strategy, long windowDurationMs) {
        this.sensorId = sensorId;
        this.normalizationStrategy = strategy;
        this.windowDurationMs = windowDurationMs;
    }

    @Override
    public void process(SensorMessage message) {

        if (!sensorId.equals(message.getSensorId()) || !ACCELEROMETER.equals(message.getDataType())) {
            throw new IllegalArgumentException("Invalid message for this processor");
        }

        SensorMessage normalizedMessage =  normalize(message);

        //Delete older entries
        Instant createdTime = normalizedMessage.getCreatedTime();
        deleteElementsOutsideWindow(normalizedMessage.getSensorId(), createdTime);

        double acceleration = computeAcceleration(normalizedMessage.getData());
        accelerationWindow.addLast(new TimestampedAcceleration(acceleration, normalizedMessage.getCreatedTime()));
    }

    private SensorMessage normalize(SensorMessage message) {
        return normalizationStrategy.normalize(message);
    }


    //I found that a major downside of this approach is the fact that once messages stop coming, you wont be able to delete what remains inside
    //A fallback is needed maybe leveraging Instant now.
    private void deleteElementsOutsideWindow(String sensorId, Instant createdTime) {
        Instant startOfTimeWindow = createdTime.minusMillis(windowDurationMs);

        ConcurrentLinkedDeque<TimestampedAcceleration> window = accelerationWindow;

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
        ConcurrentLinkedDeque<TimestampedAcceleration> window = accelerationWindow;
        if (window.isEmpty()) {
            return 0.0;
        }
        deleteElementsOutsideWindow(sensorId, window.getLast().getTimestamp());
        return window.stream().map(timestampedAcceleration -> timestampedAcceleration.getAcceleration())
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }


}
