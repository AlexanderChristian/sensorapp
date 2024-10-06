package com.example.sensorapp;

import com.example.sensorapp.Domain.Consumers.AccelerometerDataProcessor;
import com.example.sensorapp.Domain.Consumers.DataProcessor;
import com.example.sensorapp.Domain.Producers.AccelerometerSensor;
import com.example.sensorapp.Domain.Producers.SensorProducer;
import com.example.sensorapp.Domain.Common.SensorMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static com.example.sensorapp.Domain.Constants.ACCELEROMETER;

public class Simulator {
    public static final int MESSAGE_BATCH_SIZE = 20;
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();
    private final DataProcessor dataProcessor = new AccelerometerDataProcessor(60);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
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
        BlockingQueue<SensorMessage> queue = new LinkedBlockingQueue<>(1200);
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

    private void computeAndOutputAverages() {
        Map<String, Double> averages = new HashMap<>();

        for (Map.Entry<String, Queue<SensorMessage>> streamPerSensor : sensorStreams.entrySet()) {
            Queue<SensorMessage> sensorMessageStream = streamPerSensor.getValue();
            String sensorId = streamPerSensor.getKey();

            //We need to add proper polling to not re-send previous messages
            for (SensorMessage message : sensorMessageStream) {
                dataProcessor.process(message);
            }
            averages.put(sensorId, dataProcessor.getAverageAcceleration(sensorId));
        }

        outputAverages(averages);

    }

    private static void outputAverages(Map<String, Double> averages) {
        System.out.println("Average Accelerations at " + LocalDateTime.now() + ":");
        for (Map.Entry<String, Double> entry : averages.entrySet()) {
            System.out.printf("Sensor %s: %.2f m/s^2%n", entry.getKey(), entry.getValue());
        }
    }

}




