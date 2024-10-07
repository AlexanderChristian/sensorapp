package com.example.sensorapp.Repositories;


import com.example.sensorapp.Domain.Entities.SensorDataEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorDataRepository extends ElasticsearchRepository<SensorDataEntity, String> {
    List<SensorDataEntity> findBySensorId(String sensorId);
}

