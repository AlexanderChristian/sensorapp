package com.example.sensorapp.domain.normalization;

import com.example.sensorapp.domain.common.SensorMessage;

public interface NormalizationStrategy {

    SensorMessage normalize(SensorMessage sensorMessage);
}
