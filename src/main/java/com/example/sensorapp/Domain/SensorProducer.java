package com.example.sensorapp.Domain;

import java.util.List;

public interface SensorProducer {
    SensorMessage generateData();
    String getId();
}
