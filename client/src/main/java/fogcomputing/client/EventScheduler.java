package fogcomputing.client;

import fogcomputing.proto.Event;
import fogcomputing.proto.EventResponse;
import fogcomputing.util.GrpcToSqliteLogger;
import fogcomputing.util.Tuple;
import io.grpc.Status;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static fogcomputing.util.UuidUtils.bytesToUUID;

public class EventScheduler implements Runnable {
    private final ThreadPoolExecutor threadPool;
    private final Stack<GrpcSensorClient> availableClients;
    private final LinkedList<Tuple<GrpcSensorClient, Future<EventResponse>>> transitTracker;
    private final GrpcToSqliteLogger logger;
    private State state;
    private int incidents;
    private int allowedInTransit;

    private int currentFailureSlowdownTime;
    private final SchedulerConfiguration configuration;
    private final Random random;

    record SchedulerConfiguration(int threadPoolSize, int requiredIncidentsForFailure, int clientTimeout,
                                  int normalStateSleepTimeMillis,
                                  int failureStateMinimumSlowdownTimeMillis, int failureStateMaximumSlowdownTimeMillis,
                                  int failureStateSlowdownStepMillis, int recoveringStateSpeedUpFactor,
                                  String remote) {}

    public EventScheduler(SchedulerConfiguration configuration, GrpcToSqliteLogger logger) {
        this.configuration = configuration;
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(configuration.threadPoolSize);
        random = new Random();

        // Initialize client pool
        availableClients = new Stack<>();
        for (int i=0; i < configuration.threadPoolSize; i++) {
            availableClients.push(new GrpcSensorClient(configuration.clientTimeout, configuration.remote));
        }

        allowedInTransit = configuration.threadPoolSize;
        transitTracker = new LinkedList<>();
        incidents = 0;
        state = State.Normal;
        this.currentFailureSlowdownTime = 0;
        this.logger = logger;
    }

    private void sleepScheduler(long sleep, boolean withJitter) {
        try {
            if (withJitter) {
                // +- 8 % jitter
                sleep = random.nextLong(Math.round(sleep*0.92), Math.round(sleep*1.08));
            }
            Thread.sleep(sleep);

        } catch (InterruptedException ignored) {}
    }

    private GrpcSensorClient findAvailableClient() {
        if (availableClients.empty()) {
            return null;
        }

        return availableClients.pop();
    }

    public Event getNextEvent() {
        int toReadFromDb = (allowedInTransit-transitTracker.size()) * 3;
        List<Event> dbEvents = logger.getUnreceivedEvents(toReadFromDb);

        for (Event dbEvent : dbEvents) {
            boolean notInTransit = true;
            for (Tuple<GrpcSensorClient, Future<EventResponse>> inTransit : transitTracker) {
                if (inTransit.first().getEvent().getUuidDatapoint().equals(dbEvent.getUuidDatapoint())) {
                    notInTransit = false;
                    break;
                }
            }

            if (notInTransit) {
                return dbEvent;
            }
        }
        return null;
    }

    private void scheduleEvents() {
        while (transitTracker.size() < allowedInTransit) {
            Event nextEvent = getNextEvent();
            if (nextEvent == null) {
                return;
            }

            GrpcSensorClient client = findAvailableClient();
            if (client == null) {
                return;
            }

            client.setEvent(nextEvent);
            client.setTimeoutMs((currentFailureSlowdownTime == 0) ?
                configuration.clientTimeout : Math.round(currentFailureSlowdownTime * 1.2));
            System.out.printf("Sending event: %s\n", bytesToUUID(nextEvent.getUuidDatapoint().toByteArray()));
            transitTracker.addLast(new Tuple<>(client, threadPool.submit(client)));
        }
    }

    private void receiveResponses() {
        LinkedList<Tuple<GrpcSensorClient, Future<EventResponse>>> processed = new LinkedList<>();

        for (Tuple<GrpcSensorClient, Future<EventResponse>> transitEvent : transitTracker) {
            if (transitEvent.second().isDone()) {
                processed.add(transitEvent);
                availableClients.push(transitEvent.first());

                try {
                    EventResponse response = transitEvent.second().get();
                    if (response.getStatus().equals("OK")) {
                        logger.acknowledgeEvent(transitEvent.first().getEvent());
                        incidents = 0;
                    } else {
                        incidents++;
                    }

                } catch (Exception ex) {
                    Status status = Status.fromThrowable(ex.getCause());
                    switch (status.getCode()) {
                        case ALREADY_EXISTS, OK -> logger.acknowledgeEvent(transitEvent.first().getEvent());
                        default -> {
                            System.err.println(status.getCode() + " error");
                            incidents++;
                        }
                    }
                }
            }
        }
        transitTracker.removeAll(processed.stream().toList());
    }

    private void stateManagement() {
        switch (state) {
            case Normal -> {
                if (incidents >= configuration.requiredIncidentsForFailure) {
                    System.out.println("[Failure] Entering state");
                    state = State.Failure;
                    allowedInTransit = 1;
                    currentFailureSlowdownTime = configuration.failureStateMinimumSlowdownTimeMillis;
                } else {
                    sleepScheduler(configuration.normalStateSleepTimeMillis, false);
                }
            }

            case Recovering -> {
                if (currentFailureSlowdownTime == 0) {
                    System.out.println("[Normal] Entering state");
                    state = State.Normal;
                } else {
                    sleepScheduler(currentFailureSlowdownTime, true);
                    currentFailureSlowdownTime -= configuration.failureStateSlowdownStepMillis * configuration.recoveringStateSpeedUpFactor;
                    if (currentFailureSlowdownTime < 0) {
                        currentFailureSlowdownTime = 0;
                    }
                    System.out.printf("[Recovering] Slowdown time is %s ms\n", currentFailureSlowdownTime);
                }
            }

            case Failure -> {
                if (incidents == 0) {
                    System.out.println("[Recovering] Entering state");
                    state = State.Recovering;
                    allowedInTransit = configuration.threadPoolSize;
                } else {
                    sleepScheduler(currentFailureSlowdownTime, true);
                    if (currentFailureSlowdownTime < configuration.failureStateMaximumSlowdownTimeMillis) {
                        currentFailureSlowdownTime += configuration.failureStateSlowdownStepMillis;
                        if (currentFailureSlowdownTime > configuration.failureStateMaximumSlowdownTimeMillis) {
                            currentFailureSlowdownTime = configuration.failureStateMaximumSlowdownTimeMillis;
                        }
                    }
                    System.out.printf("[Failure] Slowdown time is %s ms\n", currentFailureSlowdownTime);
                }
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            scheduleEvents();
            receiveResponses();
            stateManagement();
        }
    }

    private enum State {
        Normal,
        Recovering,
        Failure
    }
}
