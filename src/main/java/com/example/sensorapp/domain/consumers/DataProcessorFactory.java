package com.example.sensorapp.domain.consumers;

public class DataProcessorFactory {

    public static DataProcessor getProcessor(String sensorId, String dataType, int slidingWindowDurationMs) {
        switch (dataType) {
            case "accelerometer":
                return new AccelerometerDataProcessor(sensorId, slidingWindowDurationMs);
            default:
                throw new IllegalArgumentException("Unknown sensor type");
        }
    }
}
