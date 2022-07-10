package fogcomputing.client;

import fogcomputing.proto.Event;
import fogcomputing.proto.EventResponse;
import fogcomputing.proto.SensorGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class GrpcSensorClient implements Callable<EventResponse> {
    private final Channel channel;
    private final SensorGrpc.SensorBlockingStub client;
    @Getter @Setter
    private Event event;
    @Getter @Setter private boolean inUse = false;

    public GrpcSensorClient(int timeoutMs, String remoteHost) {
        channel = ManagedChannelBuilder.forAddress(remoteHost, 5000).usePlaintext().build();
        client = SensorGrpc.newBlockingStub(channel)
            .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public EventResponse call() {
        return client.putEvent(event);
    }

}
