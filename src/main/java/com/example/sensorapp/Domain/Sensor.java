package com.example.sensorapp.Domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Sensor implements Runnable {
    private SensorDataConsumer server = new ServerImpl();

    private String sensorId;
    private Random random = new Random();


    @Override
    public void run() {
        sensorId = "Sensor1";
        int runCount = 0;
        //implement message sending
        while(runCount < 10){
            List<Object> data = new ArrayList<>();
            data.add(random.nextDouble(10));
            data.add(random.nextDouble(10));
            data.add(random.nextDouble(10));
            SensorMessage sensorMessage = new SensorMessage(sensorId, Instant.now(),data, "accelerometer", "m/s^2");
            server.receiveData(sensorMessage);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            runCount++;
        }
    }
}
