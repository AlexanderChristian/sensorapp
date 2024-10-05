package com.example.sensorapp.Domain;

public interface DataProcessingFunction {

    void process(SensorMessage message);

    double getAverageAcceleration(String sensorId);

}
