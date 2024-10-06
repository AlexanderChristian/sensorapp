package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;

public interface DataProcessor {

    void process(SensorMessage message);

    double getAverageAcceleration(String sensorId);

}
