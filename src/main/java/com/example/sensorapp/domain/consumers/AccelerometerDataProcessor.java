package com.example.sensorapp.domain.consumers;

import com.example.sensorapp.domain.common.SensorMessage;
import com.example.sensorapp.domain.consumers.util.SlidingWindowAvg;
import com.example.sensorapp.domain.consumers.util.TimestampedAccelerationAvg;
import com.example.sensorapp.domain.normalization.AccelerometerNormalizationStrategy;
import com.example.sensorapp.domain.normalization.NormalizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.example.sensorapp.domain.Constants.ACCELEROMETER;

public class AccelerometerDataProcessor implements DataProcessor {

    private final Logger log = LoggerFactory.getLogger(AccelerometerDataProcessor.class);
    public static final int ONE_MINUTE_MS = 60000;
    private final ConcurrentLinkedDeque<TimestampedAccelerationAvg> accelerationWindow = new ConcurrentLinkedDeque<>();
    private final String sensorId;
    private long windowDurationMs = ONE_MINUTE_MS;
    private final NormalizationStrategy normalizationStrategy = new AccelerometerNormalizationStrategy();

    public AccelerometerDataProcessor(String sensorId, int slidingWindowDurationMs) {
        this.sensorId = sensorId;
        this.windowDurationMs = slidingWindowDurationMs;
    }

    @Override
    public void process(SensorMessage message) {

        if (!sensorId.equals(message.getSensorId()) || !ACCELEROMETER.equals(message.getDataType()) || message.getData().length != 3) {
            throw new IllegalArgumentException("Invalid message for this processor");
        }
        double x = 0, y = 0, z = 0;

        SensorMessage normalizedMessage = normalize(message);
        Object[] data = normalizedMessage.getData();
        if (data[0] != null && data[0] instanceof Double) {
            x = (double) data[0];
        }
        if (data[1] != null && data[1] instanceof Double) {
            y = (double) data[1];
        }
        if (data[2] != null && data[2] instanceof Double) {
            z = (double) data[2];
        }

        //Delete older entries
        Instant createdTime = normalizedMessage.getCreatedTime();
        deleteElementsOutsideWindow(normalizedMessage.getSensorId(), createdTime);

        accelerationWindow.addLast(new TimestampedAccelerationAvg(x, y, z, normalizedMessage.getCreatedTime()));
    }

    private SensorMessage normalize(SensorMessage message) {
        if (normalizationStrategy != null) {
            return normalizationStrategy.normalize(message);
        } else {
            return message;
        }
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

        window.removeIf(timestampedAcceleration -> timestampedAcceleration.timestamp().isBefore(startOfTimeWindow));
    }

    @Override
    public SlidingWindowAvg getAverageAcceleration() {
        double X = 0, Y = 0, Z = 0;
        int count = 0;

        if (accelerationWindow.isEmpty()) {
            log.info("Empty acceleration window for: " + sensorId);
            return new SlidingWindowAvg();
        }

        //Clear if one minute has passed for the last message
        TimestampedAccelerationAvg last = accelerationWindow.getLast();
        deleteElementsOutsideWindow(sensorId, last.timestamp());

        if (accelerationWindow.isEmpty()) {
            log.info("Empty acceleration window for: " + sensorId);
            return new SlidingWindowAvg();
        }

        for (TimestampedAccelerationAvg data : accelerationWindow) {
            X += data.x();
            Y += data.y();
            Z += data.z();
            count++;
        }

        double avgX = (count > 0) ? X / count : 0;
        double avgY = (count > 0) ? Y / count : 0;
        double avgZ = (count > 0) ? Z / count : 0;


        Optional<Instant> start = Optional.ofNullable(accelerationWindow.peekFirst())
                .map(TimestampedAccelerationAvg::timestamp);

        Optional<Instant> end = Optional.ofNullable(accelerationWindow.peekLast())
                .map(TimestampedAccelerationAvg::timestamp);

        return new SlidingWindowAvg(sensorId, avgX, avgY, avgZ,
                start.orElse(null),
                end.orElse(null));
    }


}
