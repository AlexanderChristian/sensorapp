package com.example.sensorapp.Controllers;

import com.example.sensorapp.Domain.Common.SensorMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
public class MeasurementsController {

    @GetMapping("/hello")
    public void hello(){
        System.out.println("Hello");
    }

    @PostMapping("/measurements")
    public void receiveData(@RequestBody List<SensorMessage> bulkMessages){
        System.out.println(bulkMessages);
    }



}
