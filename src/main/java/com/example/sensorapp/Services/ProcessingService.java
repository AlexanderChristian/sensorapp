package com.example.sensorapp.Services;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Domain.Consumers.AccelerometerDataProcessor;
import com.example.sensorapp.Domain.Consumers.DataProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProcessingService {
    private final DataProcessor dataProcessor = new AccelerometerDataProcessor(60000);
    private final Map<String, Instant> lastProcessedTimestamps = new ConcurrentHashMap<>();

    private final MeasurementIngestionService measurementService;

    @Autowired
    public ProcessingService(MeasurementIngestionService measurementService) {
        this.measurementService = measurementService;
    }


    @Scheduled(fixedRate = 5000) // Every 5s
    public void computeAndOutputAverages() {
        Map<String, Double> averages = new HashMap<>();

        for (Map.Entry<String, Queue<SensorMessage>> streamPerSensor : measurementService.getSensorStreams().entrySet()) {
            Queue<SensorMessage> sensorMessageStream = streamPerSensor.getValue();
            String sensorId = streamPerSensor.getKey();

            processNewMessages(sensorId, sensorMessageStream);

            averages.put(sensorId, dataProcessor.getAverageAcceleration(sensorId));
        }

        outputAverages(averages);
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
