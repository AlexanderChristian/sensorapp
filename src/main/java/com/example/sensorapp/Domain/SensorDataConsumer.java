package com.example.sensorapp.Domain;

import java.util.List;

public interface SensorDataConsumer {

    public void receiveData(List<SensorMessage> messageList);

}
