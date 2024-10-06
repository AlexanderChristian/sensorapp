package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;

public interface NormalizationStrategy {

    SensorMessage normalize(SensorMessage sensorMessage);
}
