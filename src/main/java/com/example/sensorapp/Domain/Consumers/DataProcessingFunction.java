package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;

public interface DataProcessingFunction {

    void process(SensorMessage message);

    double getAverageAcceleration(String sensorId);

}
