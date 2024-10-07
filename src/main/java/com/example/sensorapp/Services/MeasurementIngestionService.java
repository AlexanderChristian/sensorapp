package com.example.sensorapp.Services;

import com.example.sensorapp.Domain.Common.SensorMessage;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Scheduled(fixedRate = 5000)
    private void monitorSizeOfStreams() {
        for (Map.Entry<String, Queue<SensorMessage>> stringQueueEntry : sensorStreams.entrySet()) {
            System.out.println("Queue for sensor: " + stringQueueEntry.getKey() + " has size " + stringQueueEntry.getValue().size());
        }

    }
}
