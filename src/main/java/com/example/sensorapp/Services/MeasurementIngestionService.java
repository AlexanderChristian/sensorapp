package com.example.sensorapp.Services;

import com.example.sensorapp.Domain.Common.SensorMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MeasurementIngestionService {
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();

    public void writeMessagesToQueues(List<SensorMessage> messages) {
        for (SensorMessage message : messages) {
            String sensorId = message.getSensorId();

            sensorStreams.computeIfAbsent(sensorId, k -> new ConcurrentLinkedQueue<>());

            sensorStreams.get(sensorId).add(message);

        }
    }

    public Map<String, Queue<SensorMessage>> getSensorStreams() {
        return sensorStreams;
    }
}
