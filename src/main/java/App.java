import com.example.sensorapp.Domain.AccelerometerDataProcessor;
import com.example.sensorapp.Domain.DataProcessingFunction;
import com.example.sensorapp.Domain.SensorMessage;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class App {
    private final Map<String, Queue<SensorMessage>> sensorStreams = new ConcurrentHashMap<>();

    private final DataProcessingFunction dataProcessor = new AccelerometerDataProcessor();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

}
