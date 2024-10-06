package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.SensorMessage;

public interface NormalizationStrategy {

    SensorMessage normalize(SensorMessage sensorMessage);
}
