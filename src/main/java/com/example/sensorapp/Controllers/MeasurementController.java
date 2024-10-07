package com.example.sensorapp.Controllers;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Services.MeasurementIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api")
@RestController
public class MeasurementController {
    private MeasurementIngestionService measurementService;

    @Autowired
    public MeasurementController(MeasurementIngestionService measurementService) {
        this.measurementService = measurementService;
    }

    @PostMapping("/measurements")
    public void receiveData(@RequestBody List<SensorMessage> bulkMessages) {
        measurementService.writeMessagesToQueues(bulkMessages);
    }

}
