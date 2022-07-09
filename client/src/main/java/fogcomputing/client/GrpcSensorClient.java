package fogcomputing.client;

import com.google.protobuf.ByteString;
import fogcomputing.proto.Event;
import fogcomputing.proto.EventResponse;
import fogcomputing.proto.SensorGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;

public class GrpcSensorClient {
    private final Channel channel;
    private final SensorGrpc.SensorBlockingStub client;
    private final byte[] uuidSensorBytes;
    private final UUID uuidSensor;

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
    }

    public void sendEvent() {
        var uuid = UUID.randomUUID();

        var uuidByteBuffer = ByteBuffer.allocate(16);
        uuidByteBuffer.putLong(uuid.getMostSignificantBits());
        uuidByteBuffer.putLong(8, uuid.getLeastSignificantBits());

        Event event = Event.newBuilder().setUuidSensor(ByteString.copyFrom(uuidSensorBytes))
                .setUuidDatapoint(ByteString.copyFrom(uuidByteBuffer))
                .setVolcanoName("Example volcano")
                .setX(1).setY(1).setZ(1)
                .setDataTimestamp(Instant.now().toEpochMilli())
                .build();

        EventResponse response = client.putEvent(event);
        System.out.println(response.getStatus());
    }


    public static void main(String[] args) throws InterruptedException {
        GrpcSensorClient sensorClient = new GrpcSensorClient();
        while(true) {
           Thread.sleep(2000);
           sensorClient.sendEvent();
        }
    }
}
