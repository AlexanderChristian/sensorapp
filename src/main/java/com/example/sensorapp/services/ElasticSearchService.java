package com.example.sensorapp.services;

import com.example.sensorapp.domain.consumers.util.SlidingWindowAvg;
import com.example.sensorapp.domain.entities.SensorDataEntity;
import com.example.sensorapp.repositories.SensorDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ElasticSearchService {
    private final Logger log = LoggerFactory.getLogger(ElasticSearchService.class);
    SensorDataRepository sensorDataRepository;

    @Autowired
    public ElasticSearchService(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    public void persistAverage(SlidingWindowAvg slidingAverage) {

        String compoundId = slidingAverage.getSensorId() + "_" + slidingAverage.getStart().toString();
        if (sensorDataRepository.existsById(compoundId)) {
            log.info("Skipping " + compoundId + " it has been saved previously.");
            return ;
        }
        SensorDataEntity sensorData = SensorDataEntity.builder()
                .id(compoundId)
                .sensorId(slidingAverage.getSensorId())
                .start(slidingAverage.getStart().toString())
                .end(slidingAverage.getEnd().toString())
                .avg(SensorDataEntity.Average.builder()
                        .x(slidingAverage.getAvgX())
                        .y(slidingAverage.getAvgY())
                        .z(slidingAverage.getAvgZ())
                        .build())
                .build();

        try {
            sensorDataRepository.save(sensorData);
            log.info("Persisted to elastic search");
        } catch (Exception e) {
            log.error("Could not persist to elastic search.");
        }
    }
}
