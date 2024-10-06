package com.example.sensorapp.Services;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Domain.Consumers.AccelerometerDataProcessor;
import com.example.sensorapp.Domain.Consumers.DataProcessingFunction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MeasurementService {
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();
    private final DataProcessingFunction function = new AccelerometerDataProcessor(60000);
    public void processSensorMessages(List<SensorMessage> messages) {
        for (SensorMessage message : messages) {
            String sensorId = message.getSensorId();

            sensorStreams.computeIfAbsent(sensorId, k -> new ConcurrentLinkedQueue<>());

            sensorStreams.get(sensorId).add(message);

        }
    }

    private static void outputAverages(Map<String, Double> averages) {
        System.out.println("Average Accelerations at " + LocalDateTime.now() + ":");
        for (Map.Entry<String, Double> entry : averages.entrySet()) {
            System.out.printf("Sensor %s: %.2f m/s^2%n", entry.getKey(), entry.getValue());
        }
    }
}
