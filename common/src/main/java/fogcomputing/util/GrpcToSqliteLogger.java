package fogcomputing.util;

import com.google.protobuf.ByteString;
import fogcomputing.proto.Event;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GrpcToSqliteLogger {
    Connection connection;

    private final String sqlCreateTable = """
                CREATE TABLE IF NOT EXISTS events
                    (uuid_datapoint blob, uuid_sensor_string blob,
                    volcano_name text, x long, y long, z long,
                    data_timestamp timestamp, received_timestamp timestamp, checksum long)
                """;

    private final String sqlInsertEvent = "INSERT INTO events values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    //private final PreparedStatement preparedInsertEvent;

    private final String sqlUpdateEvent = "UPDATE events SET received_timestamp = ? WHERE uuid_datapoint = ?";
    //private final PreparedStatement preparedUpdateEvent;

    private final String sqlExistsEvent = "SELECT COUNT(*) FROM events WHERE uuid_datapoint = ?";
    private final String sqlSelectReceivedNullEvents = """
        SELECT uuid_datapoint, uuid_sensor_string, volcano_name, x, y, z, data_timestamp, checksum
        FROM events WHERE received_timestamp IS NULL
        LIMIT ?
        """;


    public GrpcToSqliteLogger() {
        this("jdbc:sqlite:sample.db");
    }

    public GrpcToSqliteLogger(String connectionString) {
        try {
            connection = DriverManager.getConnection(connectionString);
            var statement = connection.createStatement();
            statement.executeUpdate(sqlCreateTable);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void log(Event request) {
        log(request, false);
    }


    // todo check if synchronized might be needed here?
    public /*synchronized*/ void log(Event request, boolean setTimestamp) {
        try {
            var preparedInsertEvent = connection.prepareStatement(sqlInsertEvent);
            preparedInsertEvent.setBytes(1, request.getUuidDatapoint().toByteArray());
            preparedInsertEvent.setBytes(2, request.getUuidSensor().toByteArray());
            preparedInsertEvent.setString(3, request.getVolcanoName());
            preparedInsertEvent.setLong(4, request.getX());
            preparedInsertEvent.setLong(5, request.getY());
            preparedInsertEvent.setLong(6, request.getZ());
            preparedInsertEvent.setTimestamp(7, Timestamp.from(Instant.ofEpochMilli(request.getDataTimestamp())));
            if (setTimestamp) {
                preparedInsertEvent.setTimestamp(8, Timestamp.from(Instant.now()));
            } else {
                preparedInsertEvent.setNull(8, Types.TIMESTAMP);
            }
            preparedInsertEvent.setLong(9, request.getChecksum());

            preparedInsertEvent.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void acknowledgeEvent(Event request) {
        try {
            var preparedUpdateEvent = connection.prepareStatement(sqlUpdateEvent);
            preparedUpdateEvent.setTimestamp(1, Timestamp.from(Instant.now()));
            preparedUpdateEvent.setBytes(2, request.getUuidDatapoint().toByteArray());

            preparedUpdateEvent.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(Event request) {
        try {
            var preparedExistsEvent = connection.prepareStatement(sqlExistsEvent);
            preparedExistsEvent.setBytes(1, request.getUuidDatapoint().toByteArray());

            var results = preparedExistsEvent.executeQuery();
            var count = results.getInt(1);
            if (count > 1) {
                System.out.printf("Datapoint %s exists %s times\n", request.getUuidDatapoint(), count);
            }

            return count != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Event> getUnreceivedEvents(int limit) {
        try {
            var preparedGetUnreceivedEvents = connection.prepareStatement(sqlSelectReceivedNullEvents);
            preparedGetUnreceivedEvents.setInt(1, limit);
            var results = preparedGetUnreceivedEvents.executeQuery();

            var events = new ArrayList<Event>();

            while (results.next()) {
                var event = Event.newBuilder()
                    .setUuidDatapoint(ByteString.copyFrom(results.getBytes(1)))
                    .setUuidSensor(ByteString.copyFrom(results.getBytes(2)))
                    .setVolcanoName(results.getString(3))
                    .setX(results.getLong(4))
                    .setY(results.getLong(5))
                    .setZ(results.getLong(6))
                    .setDataTimestamp(results.getTimestamp(7).getTime())
                    .setChecksum(results.getLong(8))
                    .build();
                events.add(event);
            }

            return events;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
