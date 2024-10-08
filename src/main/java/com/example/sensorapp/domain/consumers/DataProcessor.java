package com.example.sensorapp.domain.consumers;

import com.example.sensorapp.domain.common.SensorMessage;
import com.example.sensorapp.domain.consumers.util.SlidingWindowAvg;

import java.util.List;

public interface DataProcessor {

    void process(SensorMessage message);

    void processBatch(List<SensorMessage> batch);

    SlidingWindowAvg getAverageAcceleration();
}
