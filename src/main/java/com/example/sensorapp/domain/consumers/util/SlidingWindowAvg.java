package com.example.sensorapp.domain.consumers.util;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SlidingWindowAvg {
    String sensorId;
    private double avgX;
    private double avgY;
    private double avgZ;
    Instant start;
    Instant end;
}