package com.example.sensorapp.Controllers;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Services.MeasurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
public class MeasurementController {

    @Autowired
    private MeasurementService measurementService;

    @PostMapping("/measurements")
    public void receiveData(@RequestBody List<SensorMessage> bulkMessages){
        measurementService.writeMessagesToQueues(bulkMessages);
    }

}
