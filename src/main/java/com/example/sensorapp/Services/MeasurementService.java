package com.example.sensorapp.Services;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Domain.Consumers.AccelerometerDataProcessor;
import com.example.sensorapp.Domain.Consumers.DataProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MeasurementService {
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();
    private final DataProcessor dataProcessor = new AccelerometerDataProcessor(60000);

    private final Map<String, Instant> lastProcessedTimestamps = new ConcurrentHashMap<>();


    public void writeMessagesToQueues(List<SensorMessage> messages) {
        for (SensorMessage message : messages) {
            String sensorId = message.getSensorId();

            sensorStreams.computeIfAbsent(sensorId, k -> new ConcurrentLinkedQueue<>());

            sensorStreams.get(sensorId).add(message);

        }
    }

    @Scheduled(fixedRate = 5000) // Every 5s
    public void computeAndOutputAverages() {
        Map<String, Double> averages = new HashMap<>();

        for (Map.Entry<String, Queue<SensorMessage>> streamPerSensor : sensorStreams.entrySet()) {
            Queue<SensorMessage> sensorMessageStream = streamPerSensor.getValue();
            String sensorId = streamPerSensor.getKey();

            processNewMessages(sensorId, sensorMessageStream);

            averages.put(sensorId, dataProcessor.getAverageAcceleration(sensorId));
        }

        outputAverages(averages);
        averages.clear();
    }

    private void processNewMessages(String sensorId, Queue<SensorMessage> sensorMessageStream) {
        Instant lastProcessed = lastProcessedTimestamps.getOrDefault(sensorId, Instant.MIN);

        while (!sensorMessageStream.isEmpty()) {
            SensorMessage message = sensorMessageStream.peek();

            if (message.getCreatedTime().isAfter(lastProcessed)) {
                dataProcessor.process(message);

                lastProcessedTimestamps.put(sensorId, message.getCreatedTime());

                sensorMessageStream.poll();
            } else {
                break;
            }
        }
    }

    private void outputAverages(Map<String, Double> averages) {
        System.out.println("Computed Averages:");
        averages.forEach((sensorId, avg) -> System.out.println("Sensor " + sensorId + ": " + avg));
    }
}
