package fogcomputing.server;

import fogcomputing.proto.Event;
import fogcomputing.proto.EventResponse;
import fogcomputing.proto.SensorGrpc;
import fogcomputing.util.ChecksumUtils;
import fogcomputing.util.GrpcToSqliteLogger;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import static fogcomputing.util.UuidUtils.bytesToUUID;

public class EventHandlerImpl extends SensorGrpc.SensorImplBase {

    private final GrpcToSqliteLogger logger;

    public EventHandlerImpl(String dbConnectionUrl) {
        this.logger = new GrpcToSqliteLogger(dbConnectionUrl);
    }

    @Override
    public void putEvent(Event request, StreamObserver<EventResponse> responseObserver) {
        try {
            System.out.printf("%s, %s,%s,%s%n", bytesToUUID(request.getUuidDatapoint().toByteArray()), request.getX(), request.getY(), request.getZ());

            // validate checksum
            if (request.getChecksum() != ChecksumUtils.checksum(request)) {
                throw new StatusRuntimeException(Status.DATA_LOSS);
            }

            if (!logger.exists(request)) {
                logger.log(request, true);
            } else {
                throw new StatusRuntimeException(Status.ALREADY_EXISTS);
            }

            EventResponse response = EventResponse.newBuilder().setStatus("OK").build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            System.err.printf("%s - %s with checksum %s\n", Status.fromThrowable(e).getCode(), request.getUuidDatapoint(), request.getChecksum());
            responseObserver.onError(e);
        }
    }
}
