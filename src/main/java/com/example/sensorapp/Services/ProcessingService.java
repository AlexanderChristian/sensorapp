package com.example.sensorapp.Services;

import com.example.sensorapp.Domain.Common.SensorMessage;
import com.example.sensorapp.Domain.Consumers.AccelerometerDataProcessor;
import com.example.sensorapp.Domain.Normalization.AccelerometerNormalizationStrategy;
import com.example.sensorapp.Domain.Consumers.DataProcessor;
import com.example.sensorapp.Domain.Consumers.Util.SlidingWindowAvg;
import jakarta.annotation.PostConstruct;
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

    public static final int SLEEP_DURATION_AFTER_PROCESSING = 1000;
    private final Logger logger = LoggerFactory.getLogger(ProcessingService.class);
    private final MeasurementIngestionService measurementService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final ExecutorService mainProcessingThreadExecutor = Executors.newSingleThreadExecutor();

    private final Map<String, DataProcessor> sensorToProcessor = new HashMap<>();

    private final ElasticSearchService elasticSearchService;

    @Autowired
    public ProcessingService(MeasurementIngestionService measurementService, ElasticSearchService elasticSearchService) {
        this.measurementService = measurementService;
        this.elasticSearchService = elasticSearchService;
    }


    @PostConstruct
    private void startMainProcessing(){
        logger.info("Main processing thread started.");
        mainProcessingThreadExecutor.submit(this::processSensorStreams);
    }

    public void processSensorStreams() {
        while (true) {
            Map<String, Queue<SensorMessage>> sensorStreams = measurementService.getSensorStreams();

            for (Map.Entry<String, Queue<SensorMessage>> entry : sensorStreams.entrySet()) {
                String sensorId = entry.getKey();
                Queue<SensorMessage> queue = entry.getValue();

                //Stop working on empty queues
                if (queue.isEmpty()) {
                    continue;
                }

                executor.submit(() -> processSensorQueue(sensorId, queue));

                try {
                    Thread.sleep(SLEEP_DURATION_AFTER_PROCESSING);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private void processSensorQueue(String sensorId, Queue<SensorMessage> queue) {
        while (!queue.isEmpty()) {
            SensorMessage message = queue.poll();
            DataProcessor dataProcessor = sensorToProcessor.computeIfAbsent(message.getSensorId(), id -> new AccelerometerDataProcessor(sensorId, new AccelerometerNormalizationStrategy()));

            dataProcessor.process(message);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void outputSensorAverages() {

        for (Map.Entry<String, DataProcessor> entry : sensorToProcessor.entrySet()) {
            String sensorId = entry.getKey();
            DataProcessor dataProcessor = entry.getValue();

            SlidingWindowAvg averageAcceleration = dataProcessor.getAverageAcceleration();

            if (averageAcceleration != null && averageAcceleration.getSensorId() != null) {
                outputAverage(averageAcceleration);
            } else {
                logger.info("No valid average data for sensor: " + sensorId);
            }
        }
    }

    private void outputAverage(SlidingWindowAvg average) {
        logger.info("Sensor ID: " + average.getSensorId() + " | Average Acceleration: " + average);
        elasticSearchService.persistAverage(average);
    }
}
