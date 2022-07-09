package fogcomputing.server;

import fogcomputing.proto.Event;
import fogcomputing.proto.EventResponse;
import fogcomputing.proto.SensorGrpc;
import fogcomputing.util.GrpcToSqliteLogger;
import io.grpc.stub.StreamObserver;

public class EventHandlerImpl extends SensorGrpc.SensorImplBase {

    private final GrpcToSqliteLogger logger;

    public EventHandlerImpl(String dbConnectionUrl) {
        this.logger = new GrpcToSqliteLogger(dbConnectionUrl);
    }

    @Override
    public void putEvent(Event request, StreamObserver<EventResponse> responseObserver) {
        try {
            System.out.printf("%s,%s,%s%n", request.getX(), request.getY(), request.getZ());

            if (!logger.exists(request)) {
                logger.log(request, true);
            }

            EventResponse response = EventResponse.newBuilder().setStatus("OK").build();
            responseObserver.onNext(response);
        } catch (Exception e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }
}
