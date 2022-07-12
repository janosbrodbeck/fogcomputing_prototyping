# Fog Computing Prototyping Assignment

Implementation of a client and server application to fulfill fog computing specific challenges.

This prototype is based on the volcano scenario from our last semester's [FogDataBench project](https://git.tu-berlin.de/fogdatabench):
The prototype's general idea and project structure is adapted from the [FogDataBench sensor](https://git.tu-berlin.de/fogdatabench/sensor).
(Which was written in Go, compared to this Java implementation and did not focus on reliability).\
We use [gRPC](https://grpc.io/) for message definition and exchange, [SQLite](https://sqlite.org/index.html) for logging events to disk.

Sensors associated with a volcano send seismic data (x,y,z) and a timestamp to the server, which logs them into its database and acknowledges them with a response.
Each sensor and data point have their own uuid, which the server uses to recognize duplicate events. The messages are checksummed to detect message corruption.
The client logs all events to disk to be able to resend them on error. On successful received response the event is marked with a timestamp on the client.
Unmarked messages in the database are also recognized and resent on client startup.\
Advanced state management on the client recognizes faulty behavior and limits sent message sending to not overload the server on reconnection or when the connection stabilizes.

## Assignment Requirements

1. Generate (simulated) environmental realistic sensor data
2. Bidirectional regular data transmissions (multiple times per minute) between client & server
3. Reliable message delivery
  - e.g. preserve data on disconnect / crash & redeliver

## Project Structure

- **client:** client implementation including data generation, transmission & failure state handling
- **common:** library for shared utilities
- **deployment:** helper scripts for server deployment on GC
- **proto:** the generated classes of the gRPC definitions for the sensor
- **server:** server implementation receiving, saving and acknowledging data events
  - **server/tcgui:** [git subtree](https://github.com/git/git/blob/30cc8d0f147546d4dd77bf497f4dec51e7265bd8/contrib/subtree/git-subtree.txt) of [tcgui](https://github.com/tum-lkn/tcgui) for visual traffic control ([tc](https://wiki.debian.org/TrafficControl)) configuration

## Prerequisites
- [Java 18](https://adoptium.net/temurin/releases/?version=18)
- [Maven 3.8+](https://maven.apache.org/)
- [Docker 20.10+](https://docs.docker.com/get-docker/)

## Building

In the repository root:

```shell
mvn package
```

## Message Reliability Promises

- Packet loss, duplication and reordering: handled by TCP
- Packet corruption: either handled by the underlying protocols, if their invariants are affected
  - Data corruption: detection handled via an Adler32 checksum (crc alternative)
- Disconnect or server crash: Clients store events locally to resend in case of errors

### Not covered

- client crash: sensor data generation is part of the client - a crash would result in not generating data
- Datastore corruption: the database is assumed as source of truth.
  Failure to write to the client database will lead to loss of new generated data as there is no in-memory fallback.


---

<details>
<summary>Notes</summary>

Note the [other branches](https://github.com/janosbrodbeck/fogcomputing_prototyping/branches) for unfinished prototypes in Rust.

</details>
