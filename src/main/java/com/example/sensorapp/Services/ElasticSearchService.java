package com.example.sensorapp.Services;

import com.example.sensorapp.Domain.Consumers.Util.SlidingWindowAvg;
import com.example.sensorapp.Domain.Entities.SensorDataEntity;
import com.example.sensorapp.Repositories.SensorDataRepository;
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

    public void persistAverage(SlidingWindowAvg slidingAverage){
        SensorDataEntity sensorData = new SensorDataEntity();
        sensorData.setId(slidingAverage.getSensorId()+"_"+slidingAverage.getStart().toString());
        sensorData.setSensorId(slidingAverage.getSensorId());

        sensorData.setStart(slidingAverage.getStart().toString());
        sensorData.setEnd(slidingAverage.getEnd().toString());

        SensorDataEntity.Average persistedAverage = new SensorDataEntity.Average();
        persistedAverage.setX(slidingAverage.getAvgX());
        persistedAverage.setY(slidingAverage.getAvgY());
        persistedAverage.setZ(slidingAverage.getAvgZ());

        sensorData.setAvg(persistedAverage);

        try {
            sensorDataRepository.save(sensorData);
            log.info("Persisted to elastic search");
        }
        catch (Exception e){
            log.error("Could not persist to elastic search.");
        }
    }
}
