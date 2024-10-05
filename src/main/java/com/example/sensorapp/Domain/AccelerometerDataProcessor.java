package com.example.sensorapp.Domain;

import java.util.Map;

public class AccelerometerDataProcessor implements DataProcessingFunction {

    @Override
    public void process(SensorMessage message) {
        System.out.println(message);
    }

    @Override
    public Map<String, Double> getAverageAccelerations() {
        return Map.of();
    }
}
