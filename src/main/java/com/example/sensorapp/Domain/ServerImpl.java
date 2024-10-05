package com.example.sensorapp.Domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerImpl implements SensorDataConsumer {
    private Map<String, AccelerometerDataProcessor> sensorToFunction = new HashMap<>();


    @Override
    public void receiveData(List<SensorMessage> message) {
        if (sensorToFunction.containsKey(message.get(0).getSensorId())){
            AccelerometerDataProcessor averageFunction = sensorToFunction.get(message.get(0).getSensorId());
            averageFunction.process(message.get());
        }
        else{
            sensorToFunction.put(message.getSensorId(), new AccelerometerDataProcessor());
        }
    }


}
