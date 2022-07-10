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
    @Setter private long timeoutMs;
    @Getter @Setter
    private Event event;

    public GrpcSensorClient(long timeoutMs, String remoteHost) {
        channel = ManagedChannelBuilder.forAddress(remoteHost, 5000).usePlaintext().build();
        client = SensorGrpc.newBlockingStub(channel);
        this.timeoutMs = timeoutMs;
    }

    @Override
    public EventResponse call() {
        return client.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).putEvent(event);
    }
}
