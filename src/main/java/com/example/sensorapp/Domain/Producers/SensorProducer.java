package com.example.sensorapp.Domain.Producers;

import com.example.sensorapp.Domain.Common.SensorMessage;
//not needed anymore on this part
public interface SensorProducer {
    SensorMessage generateData();
    String getId();
}
