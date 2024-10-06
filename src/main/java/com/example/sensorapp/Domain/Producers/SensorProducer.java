package com.example.sensorapp.Domain.Producers;

import com.example.sensorapp.Domain.Common.SensorMessage;

public interface SensorProducer {
    SensorMessage generateData();
    String getId();
}
