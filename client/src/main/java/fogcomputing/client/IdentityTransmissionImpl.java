//package fogdatabench.sensor.proxy;
//
//import fogdatabench.sensor.proto.DataSinkGrpc;
//import fogdatabench.sensor.proto.TransmissionRequest;
//import fogdatabench.sensor.proto.TransmissionResponse;
//import fogdatabench.sensor.proxy.receiver.ServerProperties;
//import fogdatabench.sensor.proxy.util.GrpcToCsvLogger;
//import io.grpc.Channel;
//import io.grpc.ManagedChannelBuilder;
//import io.grpc.stub.StreamObserver;
//
//public class IdentityTransmissionImpl extends DataSinkGrpc.DataSinkImplBase {
//
//    private final DataSinkGrpc.DataSinkFutureStub client;
//    private final GrpcToCsvLogger logger;
//
//
//    /**
//     * Defaults to sending to localhost:5000
//     */
//    public IdentityTransmissionImpl() {
//        this(new ServerProperties());
//    }
//
//    public IdentityTransmissionImpl(String addressName, int port) {
//        this(new ServerProperties(addressName, port, "testsystem", "adapter", "IN", true, ServerProperties.DEFAULT_LOGFILEPATH));
//    }
//
//    public IdentityTransmissionImpl(ServerProperties serverProperties) {
//        Channel channel = ManagedChannelBuilder.forAddress(serverProperties.getHost(), serverProperties.getPort())
//                .usePlaintext().build();
//        client = DataSinkGrpc.newFutureStub(channel);
//        logger = new GrpcToCsvLogger(serverProperties);
//    }
//
//    @Override
//    public void transmission(TransmissionRequest request, StreamObserver<TransmissionResponse> responseObserver) {
//        try {
//            logger.log(request);
//            TransmissionResponse response = TransmissionResponse.newBuilder().setStatus("OK").build();
//            responseObserver.onNext(response);
//
//            client.transmission(TransmissionRequest.newBuilder(request).build());
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(e);
//        }
//    }
//}
