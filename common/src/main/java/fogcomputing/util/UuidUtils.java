package fogcomputing.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class UuidUtils {

    private UuidUtils() {}

    public static byte[] uuidToBytes(UUID uuid) {
        var uuidByteBuffer = ByteBuffer.allocate(16);
        uuidByteBuffer.putLong(uuid.getMostSignificantBits());
        uuidByteBuffer.putLong(8, uuid.getLeastSignificantBits());
        return uuidByteBuffer.array();
    }

    public static UUID bytesToUUID(byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Expected byte array has to be of length 16!");
        }
        var mostSignificant = ByteBuffer.allocate(8);
        mostSignificant.put(bytes, 0, 8);
        mostSignificant.position(0);
        var leastSignificant = ByteBuffer.allocate(8);
        leastSignificant.put(bytes, 8, 8);
        leastSignificant.position(0);

        return new UUID(mostSignificant.getLong(), leastSignificant.getLong());
    }

}
