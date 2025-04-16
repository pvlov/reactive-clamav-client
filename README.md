This project is a **Proof of Concept (PoC)** implementation of a client for interacting with the ClamAV daemon. It is designed to demonstrate how to use reactive programming with Project Reactor to perform asynchronous communication with the ClamAV server. The client supports scanning data streams and checking the availability of the ClamAV daemon.

## Disclaimer

This project is **not production-ready**. It is intended solely as a reference or starting point for further development. Use it at your own risk, and ensure proper testing and validation before using it in any production environment.

## Features

- **Reactive Programming**: Built using Project Reactor for non-blocking, asynchronous operations.
- **End-to-End Scanning**: Supports scanning data streams for potential threats using the ClamAV `INSTREAM` command.
- **Health Check**: Includes a method to verify the availability of the ClamAV daemon using the `PING` command.
- **Connection Pooling**: Utilizes connection pooling for efficient resource management.

## Testing

The project includes **rudimentary end-to-end tests** that verify the functionality of the client using [Testcontainers](https://www.testcontainers.org/). These tests spin up a ClamAV container to simulate a real ClamAV daemon and validate the scanning and health check features.

To run the tests:

```bash
./gradlew test
```

## Benchmarking

The project also includes some basic **benchmarking** code to measure the performance of the client. The benchmarks are not exhaustive and should be used as a starting point for performance evaluation.
The benchmarking code will use 5MiB of data to simulate a file scan. 

To run the benchmarks, first ensure that ClamAV is running on `localhost:3310` and then execute the following command:

```bash
./gradlew jmh
```

You might want to run ClamAV using Docker for testing purposes. You can do this with the following command:

```bash
docker run --rm -d mkodockx/docker-clamav:alpine
```

## Requirements

- Java 21
- Gradle
- Docker (for running Testcontainers)

## Limitations

- The project is a Proof of Concept and lacks production-grade features such as robust error handling, logging, and configuration management.
- The tests are basic and may not cover all edge cases.

## License

This project is licensed under the [MIT License](LICENSE).

