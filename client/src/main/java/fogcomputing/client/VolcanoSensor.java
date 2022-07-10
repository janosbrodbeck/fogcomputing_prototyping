package fogcomputing.client;

import com.google.protobuf.ByteString;
import fogcomputing.proto.Event;
import fogcomputing.util.ChecksumUtils;
import fogcomputing.util.GrpcToSqliteLogger;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class VolcanoSensor implements Runnable {
    private final String volcanoName;
    private final byte[] uuidSensor;
    private final GrpcToSqliteLogger logger;
    private long frequency;
    private final Random random;

    public VolcanoSensor(long frequency, String volcanoName, UUID uuidSensor, GrpcToSqliteLogger logger) {
        this.volcanoName = volcanoName;
        this.uuidSensor = uuidToBytes(uuidSensor);
        this.logger = logger;
        this.frequency = frequency;
        this.random = new Random();
    }

    private byte[] uuidToBytes(UUID uuid) {
        var uuidByteBuffer = ByteBuffer.allocate(16);
        uuidByteBuffer.putLong(uuid.getMostSignificantBits());
        uuidByteBuffer.putLong(8, uuid.getLeastSignificantBits());
        return uuidByteBuffer.array();
    }

    public Event nextEvent() {
        var uuidDatapoint = uuidToBytes(UUID.randomUUID());

        Event.Builder eventBuilder = Event.newBuilder()
            .setUuidSensor(ByteString.copyFrom(uuidSensor))
            .setUuidDatapoint(ByteString.copyFrom(uuidDatapoint))
            .setVolcanoName(this.volcanoName)
            .setX(random.nextInt()).setY(random.nextInt()).setZ(random.nextInt())
            .setDataTimestamp(Instant.now().toEpochMilli());

        eventBuilder.setChecksum(ChecksumUtils.checksum(eventBuilder));

        return eventBuilder.build();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(frequency);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.log(nextEvent());

        }
    }
}
