package com.example.sensorapp;

import com.example.sensorapp.Domain.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class Simulator {
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();
    private final DataProcessingFunction dataProcessor = new AccelerometerDataProcessor();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Random random = new Random();
    private final List<String> sensorIds = Arrays.asList("ACC001", "ACC002", "ACC003"); // Example sensor IDs


    public void run() {
        scheduler.scheduleAtFixedRate(this::generateSensorData, 0, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::computeAndOutputAverages, 0, 5, TimeUnit.SECONDS);
    }

    private void generateSensorData() {
        for (String sensorId : sensorIds) {
            SensorMessage message = new SensorMessage(
                    sensorId,
                    Instant.now(),
                    new Object[]{random.nextDouble(),random.nextDouble(),random.nextDouble()},
                    "ACCELEROMETER",
                    "m/s^2"
            );

            sensorStreams.computeIfAbsent(sensorId, k -> new ConcurrentLinkedQueue<>()).offer(message);

            Queue<SensorMessage> queue = sensorStreams.get(sensorId);
            while (queue.size() > 20) { // check happens every second
                queue.poll();
            }
        }
    }

    private void computeAndOutputAverages() {
        Map<String, Double> averages = new HashMap<>();

        for (Map.Entry<String, Queue<SensorMessage>> entry : sensorStreams.entrySet()) {
            String sensorId = entry.getKey();
            Queue<SensorMessage> messages = entry.getValue();

            for (SensorMessage message : messages) {
                dataProcessor.process(message);
            }

            averages.put(sensorId, dataProcessor.getAverageAcceleration(sensorId));
        }

        System.out.println("Average Accelerations at " + LocalDateTime.now() + ":");
        for (Map.Entry<String, Double> entry : averages.entrySet()) {
            System.out.printf("Sensor %s: %.2f m/s^2%n", entry.getKey(), entry.getValue());
        }
        System.out.println();
    }

}


