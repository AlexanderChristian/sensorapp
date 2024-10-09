package com.example.sensorapp.controllers;

import com.example.sensorapp.domain.common.SensorMessage;
import com.example.sensorapp.services.MeasurementIngestionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api")
@RestController
public class MeasurementController {
    private static final Logger logger = LoggerFactory.getLogger(MeasurementController.class);
    private final MeasurementIngestionService measurementService;

    @Autowired
    public MeasurementController(MeasurementIngestionService measurementService) {
        this.measurementService = measurementService;
    }

    @PostMapping("/measurements")
    public ResponseEntity<String> receiveData(@RequestBody List<@Valid SensorMessage> bulkMessages) {
        measurementService.writeMessagesToQueues(bulkMessages);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Messages successfully processed");

    }

    @PostMapping("/measurement")
    public ResponseEntity<String> receiveData(@RequestBody @Valid SensorMessage message) {
        measurementService.writeMessagesToQueues(List.of(message));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Message successfully processed");
    }

}
