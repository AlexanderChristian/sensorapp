package com.example.sensorapp.Domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Sensor implements Runnable {
    private Server server = new ServerImpl();

    private String sensorId;
    private Random random = new Random();

    @Override
    public void run() {
        sensorId = "Sensor1";
        int runCount = 0;
        //implement message sending
        while(runCount < 10){
            List<Object> data = new ArrayList<>();
            data.add(-1.02);
            data.add(1.32);
            data.add(1.5);
            SensorMessage sensorMessage = new SensorMessage(sensorId, Instant.now(),data, "accelerometer", "m/s^2");
            server.receiveMessage(sensorMessage);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            runCount++;
        }
    }
}
