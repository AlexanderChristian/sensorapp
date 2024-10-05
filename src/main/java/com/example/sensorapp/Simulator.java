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
    private final ExecutorService sensorExecutor = Executors.newCachedThreadPool();
    private final Random random = new Random();
    private List<SensorProducer> sensors = new ArrayList<>();


    public void run() {
        sensors.add(new AccelerometerSensor("ACC001", ACCELEROMETER, "g-force"));
        sensors.add(new AccelerometerSensor("ACC002", ACCELEROMETER, "m/s^2"));
        for (SensorProducer sensor : sensors)
        sensorExecutor.submit(() -> runSensor(sensor));
        scheduler.scheduleAtFixedRate(this::computeAndOutputAverages, 0, 5, TimeUnit.SECONDS);
    }

    private void runSensor(SensorProducer sensor) {
        BlockingQueue<SensorMessage> queue = new LinkedBlockingQueue<>();
        sensorStreams.put(sensor.getId(), queue);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                SensorMessage message = sensor.generateData();
                queue.offer(message);

                // Only interested in the last minute, can be modified with a batch size or different time if needed
                while (queue.size() > 20 * 60) {
                    queue.poll();
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
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


