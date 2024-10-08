package com.example.sensorapp.Domain.Normalization;

import com.example.sensorapp.Domain.Common.SensorMessage;

public interface NormalizationStrategy {

    SensorMessage normalize(SensorMessage sensorMessage);
}
