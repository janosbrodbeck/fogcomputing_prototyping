package fogcomputing.client;

import com.google.protobuf.ByteString;
import fogcomputing.proto.Event;
import fogcomputing.proto.EventResponse;
import fogcomputing.proto.SensorGrpc;
import fogcomputing.util.GrpcToSqliteLogger;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class GrpcSensorClient {
    private final Channel channel;
    private final SensorGrpc.SensorBlockingStub client;
    private final byte[] uuidSensorBytes;
    private final UUID uuidSensor;
    private final GrpcToSqliteLogger logger;

    public GrpcSensorClient() {
        {
            uuidSensor = UUID.randomUUID();
            var uuidByteBuffer = ByteBuffer.allocate(16);
            uuidByteBuffer.putLong(uuidSensor.getMostSignificantBits());
            uuidByteBuffer.putLong(8, uuidSensor.getLeastSignificantBits());
            uuidSensorBytes = uuidByteBuffer.array();
        }

        channel = ManagedChannelBuilder.forAddress("localhost", 5000).usePlaintext().build();
        client = SensorGrpc.newBlockingStub(channel);
        logger = new GrpcToSqliteLogger("jdbc:sqlite:client.sqlite");

        checkForOldEventsAndResend();
    }

    void checkForOldEventsAndResend() {
        List<Event> unreceivedEvents = logger.getUnreceivedEvents();
        for (Event event : unreceivedEvents) {
            EventResponse response = client.putEvent(event);
            if (response.getStatus().equals("OK")) {
                logger.acknowledgeEvent(event);
            }
        }
    }

    public void sendEvent() {
        var uuid = UUID.randomUUID();

        var uuidByteBuffer = ByteBuffer.allocate(16);
        uuidByteBuffer.putLong(uuid.getMostSignificantBits());
        uuidByteBuffer.putLong(8, uuid.getLeastSignificantBits());

        Event event = Event.newBuilder().setUuidSensor(ByteString.copyFrom(uuidSensorBytes))
                .setUuidDatapoint(ByteString.copyFrom(uuidByteBuffer.array()))
                .setVolcanoName("Example volcano")
                .setX(1).setY(1).setZ(1)
                .setDataTimestamp(Instant.now().toEpochMilli())
                .build();

        logger.log(event);
        EventResponse response = client.putEvent(event);
        System.out.println(response.getStatus());
        if (response.getStatus().equals("OK")) {
            logger.acknowledgeEvent(event);
        }
    }


    public static void main(String[] args) throws InterruptedException {
        GrpcSensorClient sensorClient = new GrpcSensorClient();
        while (true) {
           Thread.sleep(2000);
           sensorClient.sendEvent();
        }
    }
}
