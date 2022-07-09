//package fogcomputing.util;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.time.Instant;
//
//public class GrpcToCsvLogger {
//
//    public static final char DEFAULT_DELIMITER = ',';
//    public static final String CSV_HEADER = "system" + DEFAULT_DELIMITER
//            + "component" + DEFAULT_DELIMITER
//            + "stage" + DEFAULT_DELIMITER
//            + "volcano-name" + DEFAULT_DELIMITER
//            + "sensor-name" + DEFAULT_DELIMITER
//            + "uuid" + DEFAULT_DELIMITER
//            + "x" + DEFAULT_DELIMITER
//            + "y" + DEFAULT_DELIMITER
//            + "z" + DEFAULT_DELIMITER
//            + "timestamp" + '\n';
//
//    private final Path logFile;
//    private final String valueSystem;
//    private final String valueComponent;
//    private final String valueStage;
//
//    public GrpcToCsvLogger(ServerProperties serverProperties) {
//        this(serverProperties, Paths.get(serverProperties.getLogfilepath()));
//    }
//
//    public GrpcToCsvLogger(ServerProperties serverProperties, String logFilePath) {
//        this(serverProperties, Paths.get(logFilePath));
//    }
//
//    public GrpcToCsvLogger(ServerProperties serverProperties, Path logFilePath) {
//        this.logFile = logFilePath;
//        writeCsvHeader();
//        valueSystem = serverProperties.getSystem();
//        valueComponent = serverProperties.getComponent();
//        valueStage = serverProperties.getStage();
//
//    }
//
//    private void writeCsvHeader() {
//        try {
//            Files.write(
//                logFile,
//                CSV_HEADER.getBytes(),
//                StandardOpenOption.CREATE);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // todo check if synchronized might be needed here?
//    public /*synchronized*/ void log(TransmissionRequest request) {
//        StringBuilder builder = new StringBuilder(valueSystem).append(DEFAULT_DELIMITER);
//        builder.append(valueComponent).append(DEFAULT_DELIMITER);
//        builder.append(valueStage).append(DEFAULT_DELIMITER);
//        builder.append(request.getVolcanoName()).append(DEFAULT_DELIMITER);
//        builder.append(request.getUuidSensor()).append(DEFAULT_DELIMITER);
//        builder.append(request.getUuidDatapoint()).append(DEFAULT_DELIMITER);
//        builder.append(request.getX()).append(DEFAULT_DELIMITER);
//        builder.append(request.getY()).append(DEFAULT_DELIMITER);
//        builder.append(request.getZ()).append(DEFAULT_DELIMITER);
//        builder.append(Instant.now().toEpochMilli()).append('\n');
//        try {
//            Files.write(
//                    logFile,
//                    builder.toString().getBytes(),
//                    StandardOpenOption.APPEND);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public /*synchronized*/ void log(OltpMessage message) {
//        StringBuilder builder = new StringBuilder(valueSystem).append(DEFAULT_DELIMITER);
//        builder.append(valueComponent).append(DEFAULT_DELIMITER);
//        builder.append(valueStage).append(DEFAULT_DELIMITER);
//        builder.append(message.getVolcanoName()).append(DEFAULT_DELIMITER);
//        builder.append(message.getUuidSensor()).append(DEFAULT_DELIMITER);
//        builder.append(message.getUuidDatapoint()).append(DEFAULT_DELIMITER);
//        builder.append(DEFAULT_DELIMITER); // x
//        builder.append(DEFAULT_DELIMITER); // y
//        builder.append(DEFAULT_DELIMITER); // z
//        builder.append(Instant.now().toEpochMilli()).append('\n');
//        try {
//            Files.write(
//                    logFile,
//                    builder.toString().getBytes(),
//                    StandardOpenOption.APPEND);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
