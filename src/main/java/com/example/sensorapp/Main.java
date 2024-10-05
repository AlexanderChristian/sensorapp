package com.example.sensorapp;

import com.example.sensorapp.Domain.Sensor;
import com.example.sensorapp.Domain.Server;
import com.example.sensorapp.Domain.ServerImpl;

public class Main {
    static Server server = new ServerImpl();
    static Sensor sensor = new Sensor();


    public static void main(String[] args) {
        Thread t1 = new Thread(sensor);
        t1.start();
    }

}
