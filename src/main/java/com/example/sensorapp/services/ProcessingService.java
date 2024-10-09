package com.example.sensorapp.services;

import com.example.sensorapp.domain.common.SensorMessage;
import com.example.sensorapp.domain.consumers.DataProcessor;
import com.example.sensorapp.domain.consumers.DataProcessorFactory;
import com.example.sensorapp.domain.consumers.util.SlidingWindowAvg;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ProcessingService {

    private final Map<String, DataProcessor> sensorToProcessor = new HashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ExecutorService mainProcessingThreadExecutor = Executors.newSingleThreadExecutor();
    private final MeasurementIngestionService measurementService;
    private final ElasticSearchService elasticSearchService;
    private final Logger logger = LoggerFactory.getLogger(ProcessingService.class);
    @Value("${sensor.processing.sleep-duration-millis}")
    public int SLEEP_DURATION_MILLIS;
    @Value("${sensor.processing.sliding-window-duration-millis}")
    public int SLIDING_WINDOW_DURATION_MILLIS;

    @Value("${sensor.processing.message-processing-batch-size}")
    public int MESSAGE_PROCESSING_BATCH_SIZE = 1000;


    @Autowired
    public ProcessingService(MeasurementIngestionService measurementService, ElasticSearchService elasticSearchService) {
        this.measurementService = measurementService;
        this.elasticSearchService = elasticSearchService;
    }


    @PostConstruct
    private void startMainProcessing() {
        logger.info("Main processing thread started.");
        logger.info("Sliding window configured to be " + SLIDING_WINDOW_DURATION_MILLIS);
        logger.info("Polling sleep duration configured to be " + SLEEP_DURATION_MILLIS);
        logger.info("Processing batch size configured to be " + MESSAGE_PROCESSING_BATCH_SIZE);
        mainProcessingThreadExecutor.submit(this::processSensorStreams);
    }

    public void processSensorStreams() {
        while (true) {
            Map<String, Queue<SensorMessage>> sensorStreams = measurementService.getSensorStreams();

            sensorStreams.entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).forEach(entry -> {
                String sensorId = entry.getKey();
                Queue<SensorMessage> queue = entry.getValue();
                executor.submit(() -> processSensorQueue(sensorId, queue));
            });

            try {
                Thread.sleep(SLEEP_DURATION_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }


    private void processSensorQueue(String sensorId, Queue<SensorMessage> queue) {
        while (!queue.isEmpty()) {
            List<SensorMessage> batch = new ArrayList<>();
            for (int i = 0; i < MESSAGE_PROCESSING_BATCH_SIZE && !queue.isEmpty(); i++) {
                SensorMessage message = queue.poll();
                if (message != null) {
                    batch.add(message);
                }
            }

            if (!batch.isEmpty()) {
                DataProcessor dataProcessor = sensorToProcessor.computeIfAbsent(sensorId,
                        id -> DataProcessorFactory.getProcessor(sensorId, batch.get(0).getDataType(), SLIDING_WINDOW_DURATION_MILLIS));

                dataProcessor.processBatch(batch);
            }
        }
    }

    @Scheduled(fixedRate = 5000)
    public void outputSensorAverages() {
        //this can throw concurrent modification exception rarely with streams, using forEach instead for thread safety.
        sensorToProcessor.forEach((sensorId, dataProcessor) -> {
                    SlidingWindowAvg avg = dataProcessor.getAverageAcceleration();
                    if (avg != null && avg.getSensorId() != null && avg.getStart() != null) {
                        outputAverage(avg);
                    } else {
                        logger.info("No valid average data for sensor: " + sensorId);
                    }
                });

        sensorToProcessor.keySet().stream()
                .filter(sensorId -> sensorToProcessor.get(sensorId).getAverageAcceleration() == null)
                .forEach(sensorId -> logger.info("No valid average data for sensor: " + sensorId));
    }

    private void outputAverage(SlidingWindowAvg average) {
        logger.info("Sensor ID: " + average.getSensorId() + " | Average Acceleration: " + average);
        elasticSearchService.persistAverage(average);
    }
}
