package com.example.sensorapp.Domain;

import java.util.Map;

public interface DataProcessingFunction {

    void process(SensorMessage message);
    Map<String, Double> getAverageAccelerations();

}
