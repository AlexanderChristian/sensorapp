package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccelerometerDataProcessorTest {

    private AccelerometerDataProcessor processor;
    private static final long TEST_WINDOW_DURATION_MS = 2000;

    @BeforeEach
    public void setup() {
        processor = new AccelerometerDataProcessor(TEST_WINDOW_DURATION_MS);  // Configure 2-second window
    }

    @Test
    public void testComputeAcceleration() {
        Object[] data = {3.0, 4.0, 0.0};  // X = 3, Y = 4, Z = 0, should give acceleration of 5
        double acceleration = processor.computeAcceleration(data);
        assertEquals(5.0, acceleration, 0.01, "Acceleration should be 5.0 m/s²");
    }

    @Test
    public void testSlidingWindowRemovesOldEntries() throws InterruptedException {
        String sensorId = "sensor_1";

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
    public void testAverageAccelerationWithMultipleSensors() throws InterruptedException {
        String sensorId1 = "sensor_1";
        String sensorId2 = "sensor_2";

        Instant now = Instant.now();

        // Process 2 messages for sensor 1
        SensorMessage msg1_sensor1 = new SensorMessage(sensorId1, now, new Object[]{1.0, 0.0, 0.0}, "accelerometer", "m/s²");
        SensorMessage msg2_sensor1 = new SensorMessage(sensorId1, now.plusMillis(500), new Object[]{0.0, 1.0, 0.0}, "accelerometer", "m/s²");
        processor.process(msg1_sensor1);
        processor.process(msg2_sensor1);

        // Process 1 message for sensor 2
        SensorMessage msg1_sensor2 = new SensorMessage(sensorId2, now.plusMillis(1000), new Object[]{1.0, 1.0, 1.0}, "accelerometer", "m/s²");
        processor.process(msg1_sensor2);

        // Get average accelerations for each sensor
        double avgAccelerationSensor1 = processor.getAverageAcceleration(sensorId1);
        double avgAccelerationSensor2 = processor.getAverageAcceleration(sensorId2);

        // Expected accelerations:
        double expectedAvgSensor1 = (1.0 + 1.0) / 2.0; // ~1.0 (avg of two sensor_1 messages)
        double expectedAvgSensor2 = Math.sqrt(1.0 * 1.0 + 1.0 * 1.0 + 1.0 * 1.0); // ~1.73 for sensor_2

        assertEquals(expectedAvgSensor1, avgAccelerationSensor1, 0.01, "Average for sensor 1 should be correct");
        assertEquals(expectedAvgSensor2, avgAccelerationSensor2, 0.01, "Average for sensor 2 should be correct");
    }

    @Test
    public void testEmptyWindowGivesZeroAverage() {
        String sensorId = "sensor_empty";
        // Get average for a sensor that hasn't received any messages
        double avgAcceleration = processor.getAverageAcceleration(sensorId);
        assertEquals(0.0, avgAcceleration, "Average acceleration should be 0.0 for empty window");
    }
}