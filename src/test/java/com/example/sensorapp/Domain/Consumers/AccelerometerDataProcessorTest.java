package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccelerometerDataProcessorTest {

    public static final String SENSOR_1 = "sensor_1";
    private AccelerometerDataProcessor processor;

    @BeforeEach
    public void setup() {
        processor = new AccelerometerDataProcessor(SENSOR_1, new AccelerometerNormalizationStrategy(), 2000);  // Configure 2-second window
    }

    @Test
    public void testComputeAcceleration() {
        Object[] data = {3.0, 4.0, 0.0};  // X = 3, Y = 4, Z = 0, should give acceleration of 5
        double acceleration = processor.computeAcceleration(data);
        assertEquals(5.0, acceleration, 0.01, "Acceleration should be 5.0 m/s²");
    }

    @Test
    public void testSlidingWindowRemovesOldEntries() throws InterruptedException {
        String sensorId = SENSOR_1;

        // Simulate messages over time
        Instant now = Instant.now();

        // First message at "now"
        SensorMessage msg1 = new SensorMessage(sensorId, now, new Object[]{1.0, 1.0, 1.0}, "accelerometer", "m/s²");
        processor.process(msg1);

        // Wait for 1 second
        TimeUnit.SECONDS.sleep(1);

        // Second message at "now + 1 second"
        Instant oneSecondLater = now.plusSeconds(1);
        SensorMessage msg2 = new SensorMessage(sensorId, oneSecondLater, new Object[]{2.0, 2.0, 2.0}, "accelerometer", "m/s²");
        processor.process(msg2);

        // Wait for another 2 seconds (total 3 seconds)
        TimeUnit.SECONDS.sleep(2);

        // Third message at "now + 3 seconds"
        Instant threeSecondsLater = now.plusSeconds(3);
        SensorMessage msg3 = new SensorMessage(sensorId, threeSecondsLater, new Object[]{3.0, 3.0, 3.0}, "accelerometer", "m/s²");
        processor.process(msg3);

        // At this point, only the second and third messages should be in the window (the first should be removed)
        double averageAcceleration = processor.getAverageAcceleration(sensorId);

        // Calculate expected average (only msg2 and msg3 should remain)
        double expectedAccelerationMsg2 = Math.sqrt(2.0 * 2.0 + 2.0 * 2.0 + 2.0 * 2.0); // ~3.46
        double expectedAccelerationMsg3 = Math.sqrt(3.0 * 3.0 + 3.0 * 3.0 + 3.0 * 3.0); // ~5.20
        double expectedAverage = (expectedAccelerationMsg2 + expectedAccelerationMsg3) / 2;

        assertEquals(expectedAverage, averageAcceleration, 0.01, "Average should only include last two messages in sliding window");
    }

    @Test
    public void testEmptyWindowGivesZeroAverage() {
        String sensorId = "sensor_empty";
        // Get average for a sensor that hasn't received any messages
        double avgAcceleration = processor.getAverageAcceleration(sensorId);
        assertEquals(0.0, avgAcceleration, "Average acceleration should be 0.0 for empty window");
    }
}