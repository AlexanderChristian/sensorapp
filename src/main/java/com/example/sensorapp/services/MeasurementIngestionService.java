package com.example.sensorapp.services;

import com.example.sensorapp.domain.common.SensorMessage;
import com.example.sensorapp.exceptions.CapacityExceededException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MeasurementIngestionService {
    private final Logger log = LoggerFactory.getLogger(MeasurementIngestionService.class);
    @Getter
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();
    @Value("${sensor.processing.writing-queue-capacity}")
    private int QUEUE_CAPACITY = 8000;

    public void writeMessageToQueues(SensorMessage message) {
        String sensorId = message.getSensorId();

        sensorStreams.computeIfAbsent(sensorId, k -> new LinkedBlockingQueue<>(QUEUE_CAPACITY));

        Queue<SensorMessage> sensorMessages = sensorStreams.get(sensorId);
        if (!sensorMessages.offer(message)) {
            log.warn("Queue for sensor " + sensorId + " is full. Dropping message.");
            throw new CapacityExceededException("Queue for sensor " + sensorId + " has reached capacity");
        }
    }

    public void writeMessagesToQueues(List<SensorMessage> messages) {
        messages.forEach(this::writeMessageToQueues);
    }

    @Scheduled(fixedRate = 5000)
    private void monitorSizeOfStreams() {
        sensorStreams.forEach((key, value) -> log.info("Queue for sensor: " + key + " has size " + value.size()));
    }
}
