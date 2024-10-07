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
import java.util.concurrent.*;

@Component
public class ProcessingService {
    private final DataProcessor dataProcessor = new AccelerometerDataProcessor(60000);
    private final Map<String, Instant> lastProcessedTimestamps = new ConcurrentHashMap<>();

    private final MeasurementIngestionService measurementService;

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Autowired
    public ProcessingService(MeasurementIngestionService measurementService) {
        this.measurementService = measurementService;
    }


    @Scheduled(fixedRate = 5000)
    public void processSensorStreams() {
        Map<String, Queue<SensorMessage>> sensorStreams = measurementService.getSensorStreams();

        for (Map.Entry<String, Queue<SensorMessage>> entry : sensorStreams.entrySet()) {
            String sensorId = entry.getKey();
            Queue<SensorMessage> queue = entry.getValue();

            executor.submit(() -> processSensorQueue(sensorId, queue));
        }
    }

    private void processSensorQueue(String sensorId, Queue<SensorMessage> queue) {
        while (!queue.isEmpty()) {
            SensorMessage message = queue.poll();
            if (message != null) {
                dataProcessor.process(message);
            }
        }

        double averageAcceleration = dataProcessor.getAverageAcceleration(sensorId);
        outputAverage(sensorId, averageAcceleration);
    }

    private void outputAverage(String sensorId, double average) {
        System.out.println("Sensor ID: " + sensorId + " | Average Acceleration: " + average);
    }
}
