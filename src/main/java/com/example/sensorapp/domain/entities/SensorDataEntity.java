package com.example.sensorapp.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "sensor_data")
public class SensorDataEntity {
    @Id
    private String id;
    private String start;
    private String end;
    private String sensorId;
    private Average avg;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Average {
        private double x;
        private double y;
        private double z;
    }
}
