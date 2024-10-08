package com.example.sensorapp.services;

import com.example.sensorapp.domain.common.SensorMessage;
import com.example.sensorapp.domain.consumers.DataProcessorFactory;
import com.example.sensorapp.domain.consumers.DataProcessor;
import com.example.sensorapp.domain.consumers.util.SlidingWindowAvg;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
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



    @Autowired
    public ProcessingService(MeasurementIngestionService measurementService, ElasticSearchService elasticSearchService) {
        this.measurementService = measurementService;
        this.elasticSearchService = elasticSearchService;
    }


    @PostConstruct
    private void startMainProcessing(){
        logger.info("Main processing thread started.");
        logger.info("Sliding window configured to be"+ SLIDING_WINDOW_DURATION_MILLIS);
        logger.info("Polling sleep duration configured to be "+ SLEEP_DURATION_MILLIS);
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
                    Thread.sleep(SLEEP_DURATION_MILLIS);
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
            DataProcessor dataProcessor = sensorToProcessor.computeIfAbsent(sensorId, id -> DataProcessorFactory.getProcessor(message.getSensorId(), message.getDataType(), SLIDING_WINDOW_DURATION_MILLIS));

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
