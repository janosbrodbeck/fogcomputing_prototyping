package fogcomputing.client;

import fogcomputing.proto.Event;
import fogcomputing.proto.EventResponse;
import fogcomputing.util.GrpcToSqliteLogger;
import fogcomputing.util.Tuple;
import io.grpc.Grpc;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class EventScheduler implements Runnable {
    private final ThreadPoolExecutor threadPool;
    private final GrpcSensorClient[] clientPool; // todo maybe List of only free clients?
    private final LinkedList<Tuple<GrpcSensorClient, Future<EventResponse>>> transitTracker;
    private final GrpcToSqliteLogger logger;
    private State state;
    private int incidents;
    private int allowedInTransit;
    private final SchedulerConfiguration configuration;

    record SchedulerConfiguration(int threadPoolSize, int requiredIncidentsForFailure, int clientTimeout,
                                  String remote) {}

    public EventScheduler(SchedulerConfiguration configuration, GrpcToSqliteLogger logger) {
        this.configuration = configuration;
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(configuration.threadPoolSize);
        clientPool = new GrpcSensorClient[configuration.threadPoolSize];

        // Initialize client pool
        for (int i=0; i < clientPool.length; i++) {
            clientPool[i] = new GrpcSensorClient(configuration.clientTimeout, configuration.remote);
        }

        allowedInTransit = configuration.threadPoolSize;
        transitTracker = new LinkedList<>();
        incidents = 0;
        state = State.Normal;
        this.logger = logger;
    }

    private GrpcSensorClient findAvailableClient() {
        for (GrpcSensorClient client : clientPool) {
            if (!client.isInUse()) {
                return client;
            }
        }
        return null;
    }

    public Event getNextEvent() {
        int toReadFromDb = (allowedInTransit-transitTracker.size()) * 3;
        List<Event> dbEvents = logger.getUnreceivedEvents(toReadFromDb);


        for (Event dbEvent : dbEvents) {
            boolean notInTransit = true;
            for (Tuple<GrpcSensorClient, Future<EventResponse>> inTransit : transitTracker) {
                // todo maybe use linkedHashMap instead
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
            client.setInUse(true);
            transitTracker.addLast(new Tuple<>(client, threadPool.submit(client)));
        }
    }

    private void receiveResponses() {
        LinkedList<Tuple<GrpcSensorClient, Future<EventResponse>>> processed = new LinkedList<>();

        for (Tuple<GrpcSensorClient, Future<EventResponse>> transitEvent : transitTracker) {
            if (transitEvent.second().isDone()) {
                processed.add(transitEvent);
                transitEvent.first().setInUse(false);
                try {
                    EventResponse response = transitEvent.second().get();
                    System.out.println(response.getStatus());
                    if (response.getStatus().equals("OK")) {
                        logger.acknowledgeEvent(transitEvent.first().getEvent());
                        incidents = 0;
                    } else {
                        incidents++;
                    }
                } catch (Exception ignored) {
                    incidents++;
                }
            }
        }
        transitTracker.removeAll(processed.stream().toList());
    }

    private void handleFailureState() {
        if (state == State.Failure) {
            if (incidents == 0) {
                state = State.Normal;
                allowedInTransit = configuration.threadPoolSize;
            }
        }

        if (state == State.Normal && incidents >= configuration.requiredIncidentsForFailure) {
            state = State.Failure;
            allowedInTransit = 1;
        }
    }

    @Override
    public void run() {
        while (true) {
            scheduleEvents();
            receiveResponses();
            handleFailureState();
        }
    }

    private enum State {
        Normal,
        Failure
    }
}

