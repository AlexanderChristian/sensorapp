package com.example.sensorapp.controllers;

import com.example.sensorapp.domain.common.SensorMessage;
import com.example.sensorapp.services.MeasurementIngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeasurementController.class)
public class MeasurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeasurementIngestionService measurementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testReceiveData_ValidSensorMessage() throws Exception {
        SensorMessage message = new SensorMessage();
        message.setSensorId("SENSOR001");
        message.setCreatedTime(Instant.now());
        message.setData(new Object[]{1.2, 2.3, 3.4});
        message.setDataType("accelerometer");
        message.setDataUnit("m/s^2");

        doNothing().when(measurementService).writeMessageToQueues(any());

        mockMvc.perform(post("/api/measurement")
                        .header("Sensor-ID", "SENSOR001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Message successfully processed"))
                .andDo(print());

        verify(measurementService, times(1)).writeMessagesToQueues(anyList());
    }

    @Test
    public void testReceiveData_InvalidSensorMessage() throws Exception {
        SensorMessage message = new SensorMessage();
        message.setSensorId(null);  // Invalid field (null)
        message.setCreatedTime(Instant.now());
        message.setData(new Object[]{1.2, 2.3});  // Invalid (less than 3)
        message.setDataType("accelerometer");
        message.setDataUnit("m/s^2");

        mockMvc.perform(post("/api/measurement")
                        .header("Sensor-ID", "")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().is5xxServerError())
                .andDo(print());

        verify(measurementService, times(0)).writeMessagesToQueues(anyList());
    }

    @Test
    public void testReceiveBulkData_ValidSensorMessages() throws Exception {
        SensorMessage message1 = new SensorMessage();
        message1.setSensorId("SENSOR001");
        message1.setCreatedTime(Instant.now());
        message1.setData(new Object[]{1.2, 2.3, 3.4});
        message1.setDataType("accelerometer");
        message1.setDataUnit("m/s^2");

        SensorMessage message2 = new SensorMessage();
        message2.setSensorId("SENSOR001");
        message2.setCreatedTime(Instant.now());
        message2.setData(new Object[]{3.2, 4.3, 5.4});
        message2.setDataType("accelerometer");
        message2.setDataUnit("m/s^2");

        doNothing().when(measurementService).writeMessageToQueues(any());

        mockMvc.perform(post("/api/measurements")
                        .header("Sensor-ID", "SENSOR001")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(message1, message2))))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Messages successfully processed"))
                .andDo(print());

        verify(measurementService, times(1)).writeMessagesToQueues(anyList());
    }

    @Test
    public void testReceiveBulkData_InvalidSensorMessages() throws Exception {
        SensorMessage message1 = new SensorMessage();
        message1.setSensorId(null);  // Invalid field (null)
        message1.setCreatedTime(Instant.now());
        message1.setData(new Object[]{1.2, 2.3});  // Invalid (less than 3)
        message1.setDataType("accelerometer");
        message1.setDataUnit("m/s^2");

        SensorMessage message2 = new SensorMessage();
        message2.setSensorId("SENSOR002");
        message2.setCreatedTime(Instant.now());
        message2.setData(new Object[]{3.2, 4.3, 5.4});
        message2.setDataType("accelerometer");
        message2.setDataUnit("m/s^2");

        mockMvc.perform(post("/api/measurements")
                        .header("Sensor-ID", "SENSOR001")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(message1, message2))))
                .andExpect(status().is5xxServerError())
                .andDo(print());

        verify(measurementService, times(0)).writeMessagesToQueues(anyList());
    }
}