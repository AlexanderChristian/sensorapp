package com.example.sensorapp.domain.consumers;

import com.example.sensorapp.domain.common.SensorMessage;
import com.example.sensorapp.domain.consumers.util.SlidingWindowAvg;

public interface DataProcessor {

    void process(SensorMessage message);

    SlidingWindowAvg getAverageAcceleration();

}
