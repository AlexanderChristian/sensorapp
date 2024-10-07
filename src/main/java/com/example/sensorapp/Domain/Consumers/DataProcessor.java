package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Domain.Consumers.Util.SlidingWindowAvg;

public interface DataProcessor {

    void process(SensorMessage message);

    SlidingWindowAvg getAverageAcceleration();

}
