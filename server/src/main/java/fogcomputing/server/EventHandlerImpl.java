package fogcomputing.server;

import fogcomputing.proto.Event;
import fogcomputing.proto.EventResponse;
import fogcomputing.proto.SensorGrpc;
import io.grpc.stub.StreamObserver;

public class EventHandlerImpl extends SensorGrpc.SensorImplBase {

    @Override
    public void putEvent(Event request, StreamObserver<EventResponse> responseObserver) {
        try {
            System.out.printf("%s,%s,%s%n", request.getX(), request.getY(), request.getZ());
            // todo write to file

            EventResponse response = EventResponse.newBuilder().setStatus("OK").build();
            responseObserver.onNext(response);
        } catch (Exception e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }
}
