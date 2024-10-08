package com.example.sensorapp.Services;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Domain.Consumers.AccelerometerDataProcessor;
import com.example.sensorapp.Domain.Normalization.AccelerometerNormalizationStrategy;
import com.example.sensorapp.Domain.Consumers.DataProcessor;
import com.example.sensorapp.Domain.Consumers.Util.SlidingWindowAvg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ProcessingService {

    private final Logger logger = LoggerFactory.getLogger(ProcessingService.class);
    private final MeasurementIngestionService measurementService;

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final Map<String, DataProcessor> sensorToProcessor = new HashMap<>();

    private final ElasticSearchService elasticSearchService;

    @Autowired
    public ProcessingService(MeasurementIngestionService measurementService, ElasticSearchService elasticSearchService) {
        this.measurementService = measurementService;
        this.elasticSearchService = elasticSearchService;
    }


    @Scheduled(fixedRate = 5000)
    public void processSensorStreams() {
        Map<String, Queue<SensorMessage>> sensorStreams = measurementService.getSensorStreams();

        for (Map.Entry<String, Queue<SensorMessage>> entry : sensorStreams.entrySet()) {
            String sensorId = entry.getKey();
            Queue<SensorMessage> queue = entry.getValue();

            //Stop working on empty queues
            if (queue.isEmpty()){
                continue;
            }

            executor.submit(() -> processSensorQueue(sensorId, queue));
        }
    }

    private void processSensorQueue(String sensorId, Queue<SensorMessage> queue) {
        while (!queue.isEmpty()) {
            SensorMessage message = queue.poll();
            DataProcessor dataProcessor = sensorToProcessor.computeIfAbsent(message.getSensorId(), id -> new AccelerometerDataProcessor(sensorId, new AccelerometerNormalizationStrategy()));

            dataProcessor.process(message);
        }

        DataProcessor dataProcessor = sensorToProcessor.get(sensorId);
        SlidingWindowAvg averageAcceleration = dataProcessor.getAverageAcceleration();

        if (null == averageAcceleration.getSensorId()){
            logger.info("Nothing to persist.");
            return;
        }
        outputAverage(averageAcceleration);
    }

    private void outputAverage(SlidingWindowAvg average) {
        logger.info("Sensor ID: " + average.getSensorId() + " | Average Acceleration: " + average);
        elasticSearchService.persistAverage(average);
    }
}
