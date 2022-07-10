//package fogdatabench.sensor.proxy;
//
//import fogdatabench.sensor.proxy.receiver.SensorGRPCServer;
//import fogdatabench.sensor.proxy.receiver.ServerProperties;
//import fogdatabench.sensor.proxy.util.ConfigUtils;
//
//import java.io.IOException;
//
//public class ApacheConnector {
//
//    public static void main( String[] args ) throws IOException, InterruptedException {
//        ServerProperties proxyProperties = ConfigUtils.getProperties("proxy", ServerProperties.class);
//        ServerProperties properties = ConfigUtils.getProperties("sut", ServerProperties.class);
//        System.out.println("Properties - " + properties.getHost() + ":"+properties.getPort());
//
//        final SensorGRPCServer server = new SensorGRPCServer(proxyProperties.getHost(), proxyProperties.getPort())
//                .addService(new fogdatabench.sensor.proxy.IdentityTransmissionImpl(properties))
//                .start();
//        server.blockUntilShutdown();
//    }
//
//}
