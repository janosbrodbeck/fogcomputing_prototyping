package fogcomputing.util;

import fogcomputing.proto.EventOrBuilder;

import java.nio.ByteBuffer;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class ChecksumUtils {

    public static long checksum(EventOrBuilder event) {
        return checksum(
            event.getUuidSensor().toByteArray(),
            event.getVolcanoName(),
            event.getUuidDatapoint().toByteArray(),
            event.getX(), event.getY(), event.getZ(),
            event.getDataTimestamp()
        );
    }

    public static long checksum(byte[] uuidSensor, String volcanoName, byte[] uuidData, long x, long y, long z, long timestamp) {
        Checksum checksum = new Adler32();
        checksum.update(uuidSensor);
        checksum.update(volcanoName.getBytes());
        checksum.update(uuidData);

        ByteBuffer toChecksum = ByteBuffer.allocate(32);
        toChecksum.putLong(x)
            .putLong(y)
            .putLong(z)
            .putLong(timestamp);

        checksum.update(toChecksum.array());

        return checksum.getValue();
    }
}
