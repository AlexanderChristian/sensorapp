package com.example.sensorapp.Repositories;


import com.example.sensorapp.Domain.SensorDataEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SensorDataRepository extends ElasticsearchRepository<SensorDataEntity, String> {
    List<SensorDataEntity> findBySensorId(String sensorId);
    }

