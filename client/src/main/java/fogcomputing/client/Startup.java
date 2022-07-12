package fogcomputing.client;

import fogcomputing.util.ConfigUtils;
import fogcomputing.util.GrpcToSqliteLogger;

import java.util.UUID;

public class Startup {
    public static void main(String[] args) throws InterruptedException {
        ClientProperties properties = ConfigUtils.getProperties("client", ClientProperties.class);
        System.out.println("Client properties: " + properties.toString());
        GrpcToSqliteLogger logger = new GrpcToSqliteLogger("jdbc:sqlite:client.sqlite");
        EventScheduler.SchedulerConfiguration schedulerConfiguration = new EventScheduler.SchedulerConfiguration(
            2, 3, 3000, 10,500,
            15000, 1000, 3,
            properties.getRemoteHost());

        VolcanoSensor volcanoSensor = new VolcanoSensor(
            3000, properties.getVolcanoName(), UUID.nameUUIDFromBytes(properties.getVolcanoName().getBytes()), logger);
        Thread sensorThread = new Thread(volcanoSensor);

        EventScheduler scheduler = new EventScheduler(schedulerConfiguration, logger);
        Thread schedulerThread = new Thread(scheduler);

        sensorThread.start();
        schedulerThread.start();

        sensorThread.join();
        schedulerThread.join();
    }
}
