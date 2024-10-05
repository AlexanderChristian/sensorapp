package com.example.sensorapp.Domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerImpl implements Server {
    private Map<String, AverageFunction> sensorToFunction = new HashMap<>();


    @Override
    public void receiveMessage(SensorMessage message) {
        if (sensorToFunction.containsKey(message.getSensorId())){
            AverageFunction averageFunction = sensorToFunction.get(message.getSensorId());
            averageFunction.process(message);
        }
        else{
            sensorToFunction.put(message.getSensorId(), new AverageFunction());
        }
    }

    @Override
    public void receiveMessageBulk(List<SensorMessage> messages) {

    }
}
