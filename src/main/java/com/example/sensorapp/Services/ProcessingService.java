package com.example.sensorapp.Services;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Domain.Consumers.AccelerometerDataProcessor;
import com.example.sensorapp.Domain.Consumers.AccelerometerNormalizationStrategy;
import com.example.sensorapp.Domain.Consumers.DataProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ProcessingService {
    private final MeasurementIngestionService measurementService;

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final Map<String, DataProcessor> sensorToProcessor = new HashMap<>();

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
            DataProcessor dataProcessor = sensorToProcessor.computeIfAbsent(message.getSensorId(), id -> new AccelerometerDataProcessor(sensorId, new AccelerometerNormalizationStrategy()));

            dataProcessor.process(message);
        }

        DataProcessor dataProcessor = sensorToProcessor.get(sensorId);
        double averageAcceleration = dataProcessor.getAverageAcceleration(sensorId);
        outputAverage(sensorId, averageAcceleration);
    }

    private void outputAverage(String sensorId, double average) {
        System.out.println("Sensor ID: " + sensorId + " | Average Acceleration: " + average);
    }
}
