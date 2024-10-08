package com.example.sensorapp.services;

import com.example.sensorapp.domain.common.SensorMessage;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MeasurementIngestionService {
    private final Logger log = LoggerFactory.getLogger(MeasurementIngestionService.class);
    @Getter
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();

    public void writeMessageToQueues(SensorMessage message) {
        String sensorId = message.getSensorId();

        sensorStreams.computeIfAbsent(sensorId, k -> new ConcurrentLinkedQueue<>());

        sensorStreams.get(sensorId).add(message);
    }

    public void writeMessagesToQueues(List<SensorMessage> messages) {
        messages.forEach(this::writeMessageToQueues);
    }

    @Scheduled(fixedRate = 5000)
    private void monitorSizeOfStreams() {
        sensorStreams.entrySet().forEach(entry -> log.info("Queue for sensor: " + entry.getKey() + " has size " + entry.getValue().size()));
    }
}
