package fogcomputing.server;

import fogcomputing.proto.SensorGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SensorGRPCServer {

    protected final ServerBuilder<?> serverBuilder;

    @NonNull
    private final String hostname;
    private final int port;
    private Server server;

    /**
     * Default server running on hostname localhost and port 5000
     */
    public SensorGRPCServer() {
        this("localhost", 5000);
    }

    public SensorGRPCServer(@NonNull String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.serverBuilder = ServerBuilder.forPort(this.port);

    }

    public SensorGRPCServer addService(SensorGrpc.SensorImplBase service) {
        this.serverBuilder.addService(service);
        return this;
    }

    public SensorGRPCServer start() throws IOException {
        server = this.serverBuilder
            .build()
            .start();
        System.out.println("Listening on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                SensorGRPCServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
        return this;
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final SensorGRPCServer server = new SensorGRPCServer()
                .addService(new DebugEventImpl())
                .start();
        server.blockUntilShutdown();
    }

}
