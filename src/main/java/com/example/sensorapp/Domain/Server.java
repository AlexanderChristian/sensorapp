package com.example.sensorapp.Domain;

import java.util.List;

public interface Server {

    public void receiveMessage(SensorMessage message);
    public void receiveMessageBulk(List<SensorMessage> messages);

}
