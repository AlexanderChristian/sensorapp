package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.SensorMessage;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.sensorapp.Domain.Constants.ACCELEROMETER;

public class AccelerometerDataProcessor implements DataProcessingFunction {
    private final Map<String, LinkedList<Double>> accelerationWindows = new ConcurrentHashMap<>();


    @Override
    public void process(SensorMessage message) {
        if (!ACCELEROMETER.equals(message.getDataType())) {
            return;
        }

        double acceleration = computeAcceleration(message.getData());
        accelerationWindows.computeIfAbsent(message.getSensorId(), k -> new LinkedList<>()).addLast(acceleration);

        LinkedList<Double> window = accelerationWindows.get(message.getSensorId());
        //#TODO Do Validation at the top level to not maintain useless messages
        while (window.size() > 1200) { // 20 messages/second * 60 seconds
            window.removeFirst();
        }
    }

    private double computeAcceleration(Object[] data) {
        double x = (double) data[0];
        double y = (double) data[1];
        double z = (double) data[2];
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public double getAverageAcceleration(String sensorId) {
        LinkedList<Double> window = accelerationWindows.get(sensorId);
        if (window == null || window.isEmpty()) {
            return 0.0;
        }
        System.out.println(sensorId+" : "+window);
        return window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /*
    private void removeOldMessages(String sensorId) {
        Instant now = Instant.now();
        LinkedList<Double> valuesAssociatedWithSensor = accelerationWindows.get(sensorId);
        valuesAssociatedWithSensor.removeIf(message -> Duration.between(message.getCreatedTime(), now).toMillis() > WINDOW_DURATION_MS);
    }

     */

}
