package com.example.sensorapp.services;

import com.example.sensorapp.domain.common.SensorMessage;
import com.example.sensorapp.exceptions.CapacityExceededException;
import com.example.sensorapp.repositories.SensorDataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {ElasticsearchRepositoriesAutoConfiguration.class})
class MeasurementIngestionServiceTest {

    @Autowired
    private MeasurementIngestionService measurementIngestionService;
    private SensorMessage sensorMessage;

    @MockBean
    private SensorDataRepository sensorDataRepository;


    @BeforeEach
    void setUp() {
        sensorMessage = new SensorMessage("ACC001", Instant.now(), new Object[]{1.0, 2.0, 3.0}, "accelerometer", "m/s^2");

        ReflectionTestUtils.setField(measurementIngestionService, "QUEUE_CAPACITY", 2);
    }

    @AfterEach
    void tearDown() {
        measurementIngestionService.getSensorStreams().clear();
    }

    @Test
    void testWriteMessageToQueueSuccessfully() {
        // When
        measurementIngestionService.writeMessageToQueues(sensorMessage);

        // Then
        Queue<SensorMessage> queue = measurementIngestionService.getSensorStreams().get(sensorMessage.getSensorId());
        assertNotNull(queue);
        assertEquals(1, queue.size());
        assertEquals(sensorMessage, queue.peek());
    }

    @Test
    void testWriteMessagesToQueue() {
        // When
        measurementIngestionService.writeMessagesToQueues(List.of(sensorMessage, sensorMessage));

        // Then
        Queue<SensorMessage> queue = measurementIngestionService.getSensorStreams().get(sensorMessage.getSensorId());
        assertNotNull(queue);
        assertEquals(2, queue.size());

        CapacityExceededException exception = assertThrows(CapacityExceededException.class, () -> {
            measurementIngestionService.writeMessagesToQueues(List.of(sensorMessage));
        });
        assertEquals("Queue for sensor ACC001 has reached capacity", exception.getMessage());
    }


    @Test
    void testMonitorSizeOfStreams() {
        SensorMessage message1 = new SensorMessage("SENSOR1", Instant.now(), new Object[]{1.0, 2.0, 3.0}, "accelerometer", "m/s^2");
        SensorMessage message2 = new SensorMessage("SENSOR2", Instant.now(), new Object[]{1.0, 2.0, 3.0}, "accelerometer", "m/s^2");

        measurementIngestionService.writeMessagesToQueues(List.of(message1, message2));

        measurementIngestionService.monitorSizeOfStreams();

        assertEquals(1, measurementIngestionService.getSensorStreams().get("SENSOR1").size());
        assertEquals(1, measurementIngestionService.getSensorStreams().get("SENSOR2").size());
    }
}