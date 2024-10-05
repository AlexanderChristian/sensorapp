package com.example.sensorapp.Domain.Producers;

import com.example.sensorapp.Domain.SensorMessage;

public interface SensorProducer {
    SensorMessage generateData();
    String getId();
}
