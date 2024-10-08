package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Domain.Consumers.Util.SlidingWindowAvg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class AccelerometerDataProcessorTest {

    private AccelerometerDataProcessor processor;

    @BeforeEach
    public void setup() {
        processor = new AccelerometerDataProcessor("SENSOR001", 60000);
    }

    @Test
    public void testProcess_ValidData() {

        SensorMessage message = new SensorMessage();
        message.setSensorId("SENSOR001");
        message.setCreatedTime(Instant.now());
        message.setData(new Object[]{1.0, 2.0, 3.0});
        message.setDataType("accelerometer");


        processor.process(message);


        SlidingWindowAvg average = processor.getAverageAcceleration();
        assertEquals(1.0, average.getAvgX());
        assertEquals(2.0, average.getAvgY());
        assertEquals(3.0, average.getAvgZ());
    }

    @Test
    public void testProcess_InvalidData() {
        SensorMessage invalidMessage = new SensorMessage();
        invalidMessage.setSensorId("WRONG_SENSOR");
        invalidMessage.setCreatedTime(Instant.now());
        invalidMessage.setData(new Object[]{1.0, 2.0, 3.0});
        invalidMessage.setDataType("accelerometer");

        assertThrows(IllegalArgumentException.class, () -> processor.process(invalidMessage));
    }

    @Test
    public void testSlidingWindow_DeletionOfOldEntries() {
        Instant now = Instant.now();

        SensorMessage message1 = new SensorMessage();
        message1.setSensorId("SENSOR001");
        message1.setCreatedTime(now.minusMillis(70000));
        message1.setData(new Object[]{1.0, 2.0, 3.0});
        message1.setDataType("accelerometer");

        SensorMessage message2 = new SensorMessage();
        message2.setSensorId("SENSOR001");
        message2.setCreatedTime(now.minusMillis(5000));
        message2.setData(new Object[]{4.0, 5.0, 6.0});
        message2.setDataType("accelerometer");

        processor.process(message1);
        processor.process(message2);

        SlidingWindowAvg average = processor.getAverageAcceleration();
        assertEquals(4.0, average.getAvgX());
        assertEquals(5.0, average.getAvgY());
        assertEquals(6.0, average.getAvgZ());
    }

    @Test
    public void testEmptyWindow() {
        SlidingWindowAvg average = processor.getAverageAcceleration();

        assertEquals(0, average.getAvgX());
        assertEquals(0, average.getAvgY());
        assertEquals(0, average.getAvgZ());
        assertNull(average.getStart());
        assertNull(average.getEnd());
    }

    @Test
    public void testSlidingWindowWithMultipleEntries() {
        Instant now = Instant.now();

        SensorMessage message1 = new SensorMessage();
        message1.setSensorId("SENSOR001");
        message1.setCreatedTime(now.minusMillis(20000));
        message1.setData(new Object[]{1.0, 2.0, 3.0});
        message1.setDataType("accelerometer");

        SensorMessage message2 = new SensorMessage();
        message2.setSensorId("SENSOR001");
        message2.setCreatedTime(now.minusMillis(10000));
        message2.setData(new Object[]{4.0, 5.0, 6.0});
        message2.setDataType("accelerometer");

        SensorMessage message3 = new SensorMessage();
        message3.setSensorId("SENSOR001");
        message3.setCreatedTime(now);
        message3.setData(new Object[]{7.0, 8.0, 9.0});
        message3.setDataType("accelerometer");

        processor.process(message1);
        processor.process(message2);
        processor.process(message3);

        SlidingWindowAvg average = processor.getAverageAcceleration();
        assertEquals(4.0, average.getAvgX());
        assertEquals(5.0, average.getAvgY());
        assertEquals(6.0, average.getAvgZ());


        assertEquals(now.minusMillis(20000), average.getStart());
        assertEquals(now, average.getEnd());
    }
}
