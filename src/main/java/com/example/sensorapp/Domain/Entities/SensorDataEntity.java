package com.example.sensorapp.Domain.Entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Document(indexName = "sensor_data")
public class SensorDataEntity {
    @Id
    private String id;

    private String start;
    private String end;
    private String sensorId;
    private Average avg;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static class Average {
        private double x;
        private double y;
        private double z;


        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }
    }


    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Average getAvg() {
        return avg;
    }

    public void setAvg(Average avg) {
        this.avg = avg;
    }
}
