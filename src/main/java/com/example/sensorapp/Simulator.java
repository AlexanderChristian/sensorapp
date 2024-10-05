package com.example.sensorapp;

import com.example.sensorapp.Domain.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static com.example.sensorapp.Domain.Constants.ACCELEROMETER;

public class Simulator {
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();
    private final DataProcessingFunction dataProcessor = new AccelerometerDataProcessor();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Random random = new Random();
    private List<SensorProducer> sensors = new ArrayList<>();


    public void run() {
        sensors.add(new AccelerometerSensor("ACC001", ACCELEROMETER, "g-force"));
        sensors.add(new AccelerometerSensor("ACC002", ACCELEROMETER, "m/s^2"));
        scheduler.scheduleAtFixedRate(this::generateSensorData, 0, 50, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::computeAndOutputAverages, 0, 5, TimeUnit.SECONDS);
    }

    private void generateSensorData() {
        for (SensorProducer sensor : sensors) {
            SensorMessage sensorMessage = sensor.generateData();

            sensorStreams.computeIfAbsent(sensorMessage.getSensorId(), k -> new ConcurrentLinkedQueue<>()).offer(sensorMessage);

            Queue<SensorMessage> queue = sensorStreams.get(sensorMessage.getSensorId());
            while (queue.size() > 1200) {
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


