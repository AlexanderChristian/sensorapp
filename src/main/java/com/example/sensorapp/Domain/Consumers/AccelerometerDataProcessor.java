package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Domain.Consumers.Util.SlidingWindowAvg;
import com.example.sensorapp.Domain.Consumers.Util.TimestampedAccelerationAvg;
import com.example.sensorapp.Domain.Normalization.NormalizationStrategy;
import com.example.sensorapp.Services.MeasurementIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.example.sensorapp.Domain.Constants.ACCELEROMETER;

public class AccelerometerDataProcessor implements DataProcessor {

    private final Logger log = LoggerFactory.getLogger(AccelerometerDataProcessor.class);
    private final ConcurrentLinkedDeque<TimestampedAccelerationAvg> accelerationWindow = new ConcurrentLinkedDeque<>();
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

        if (!sensorId.equals(message.getSensorId()) || !ACCELEROMETER.equals(message.getDataType()) || message.getData().length != 3) {
            throw new IllegalArgumentException("Invalid message for this processor");
        }

        SensorMessage normalizedMessage = normalize(message);
        Object[] data = normalizedMessage.getData();
        double x = (double) data[0];
        double y = (double) data[1];
        double z = (double) data[2];

        //Delete older entries
        Instant createdTime = normalizedMessage.getCreatedTime();
        deleteElementsOutsideWindow(normalizedMessage.getSensorId(), createdTime);

        accelerationWindow.addLast(new TimestampedAccelerationAvg(x, y, z, normalizedMessage.getCreatedTime()));
    }

    private SensorMessage normalize(SensorMessage message) {
        return normalizationStrategy.normalize(message);
    }


    //I found that a major downside of this approach is the fact that once messages stop coming, you wont be able to delete what remains inside
    //A fallback is needed maybe leveraging Instant now.
    private void deleteElementsOutsideWindow(String sensorId, Instant createdTime) {
        Instant startOfTimeWindow = createdTime.minusMillis(windowDurationMs);

        ConcurrentLinkedDeque<TimestampedAccelerationAvg> window = accelerationWindow;

        Instant fallbackStartOfTimeWindow = Instant.now().minusMillis(windowDurationMs);

        // If the message timestamp is before the fallback window which uses instant as reference, clear the entire window
        if (createdTime.isBefore(fallbackStartOfTimeWindow)) {
            log.info("Message too old, clearing entire window for sensor: " + sensorId);
            window.clear();
        }

        window.removeIf(timestampedAcceleration -> timestampedAcceleration.getTimestamp().isBefore(startOfTimeWindow));
    }

    //Check thread safety
    @Override
    public SlidingWindowAvg getAverageAcceleration() {
        double X = 0, Y = 0, Z = 0;
        int count = 0;

        if (accelerationWindow.isEmpty()) {
            return new SlidingWindowAvg();
        }

        for (TimestampedAccelerationAvg data : accelerationWindow) {
            X += data.getX();
            Y += data.getY();
            Z += data.getZ();
            count++;
        }

        double avgX = (count > 0) ? X / count : 0;
        double avgY = (count > 0) ? Y / count : 0;
        double avgZ = (count > 0) ? Z / count : 0;
        Instant start = accelerationWindow.peekFirst().getTimestamp();
        Instant end = accelerationWindow.peekLast().getTimestamp();


        return new SlidingWindowAvg(sensorId, avgX, avgY, avgZ, start, end);
    }


}
