package com.example.sensorapp;

import com.example.sensorapp.Domain.*;
import com.example.sensorapp.Domain.Consumers.AccelerometerDataProcessor;
import com.example.sensorapp.Domain.Consumers.DataProcessingFunction;
import com.example.sensorapp.Domain.Producers.AccelerometerSensor;
import com.example.sensorapp.Domain.Producers.SensorProducer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static com.example.sensorapp.Domain.Constants.ACCELEROMETER;

public class Simulator {
    public static final int MESSAGE_BATCH_SIZE = 20;
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();
    private final DataProcessingFunction dataProcessor = new AccelerometerDataProcessor();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ExecutorService sensorExecutor = Executors.newCachedThreadPool();
    private final Random random = new Random();
    private List<SensorProducer> sensors = new ArrayList<>();


    public void run() {
        sensors.add(new AccelerometerSensor("ACC001", ACCELEROMETER, "g-force"));
        //Add normalization of data
        sensors.add(new AccelerometerSensor("ACC002", ACCELEROMETER, "m/s^2"));
        for (SensorProducer sensor : sensors) {
            sensorExecutor.submit(() -> runSensor(sensor));
        }
        scheduler.scheduleAtFixedRate(this::computeAndOutputAverages, 0, 5, TimeUnit.SECONDS);
    }

    private void runSensor(SensorProducer sensor) {
        BlockingQueue<SensorMessage> queue = new LinkedBlockingQueue<>();
        sensorStreams.put(sensor.getId(), queue);
        int currentMessageCount = 0;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                while (currentMessageCount < 20) {
                    SensorMessage message = sensor.generateData();
                    queue.offer(message);

                    currentMessageCount++;
                }
                currentMessageCount = 0;
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

        //#TODO Normalize to m/s
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


