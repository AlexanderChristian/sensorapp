package com.example.sensorapp.Domain.Consumers;

import com.example.sensorapp.Domain.Common.SensorMessage;

import java.util.Arrays;

public class AccelerometerNormalizationStrategy implements NormalizationStrategy {

    public static final String G_FORCE = "g-force";
    public static final String M_S_2 = "m/s^2";

    @Override
    public SensorMessage normalize(SensorMessage message) {
        if (G_FORCE.equals(message.getDataUnit())) {
            Object[] normalizedData = Arrays.stream(message.getData())
                    .map(value -> (double) value * 9.81)
                    .toArray();
            return new SensorMessage(
                    message.getSensorId(),
                    message.getCreatedTime(),
                    normalizedData,
                    message.getDataType(),
                    M_S_2
            );
        }
        return message;
    }
}
