package com.example.sensorapp.Domain.Producers;

import com.example.sensorapp.Domain.Common.SensorMessage;

import java.time.Instant;
import java.util.Random;

public class AccelerometerSensor implements SensorProducer {
    private final String sensorId;
    private final String dataType;
    private final String dataUnit;
    Random random = new Random();

    public AccelerometerSensor(String sensorId, String dataType, String dataUnit) {
        this.sensorId = sensorId;
        this.dataType = dataType;
        this.dataUnit = dataUnit;
    }

    @Override
    public SensorMessage generateData() {
        Object[] data = new Object[]{random.nextDouble(100), random.nextDouble(100), random.nextDouble(100)};
        return new SensorMessage(sensorId, Instant.now(), data, dataType, dataUnit);
    }

    @Override
    public String getId() {
        return sensorId;
    }
}

