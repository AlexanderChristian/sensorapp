package com.example.sensorapp.Domain;

public class AverageFunction implements ProcessingFunction {

    @Override
    public void process(SensorMessage message) {
        System.out.println(message);
    }
}
