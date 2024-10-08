package com.example.sensorapp.domain.common;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SensorMessage {
    @NotNull(message = "sensorId must not be null")
    private String sensorId;
    @NotNull(message = "createdTime must not be null")
    private Instant createdTime;
    @NotNull(message = "data must not be null")
    @Size(min = 3, message = "data must contain at least 3 values")
    private Object[] data;
    @NotNull(message = "dataType must not be null")
    private String dataType;
    @NotNull(message = "dataUnit must not be null")
    private String dataUnit;
}


