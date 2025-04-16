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

## Requirements

- Java 21
- Gradle
- Docker (for running Testcontainers)

## Usage

This project is intended as a reference implementation. To use it:

1. Clone the repository.
2. Build the project using Gradle:
   ```bash
   ./gradlew build
   ```
3. Run the tests:
   ```bash
   ./gradlew test
   ```

## Limitations

- The project is a Proof of Concept and lacks production-grade features such as robust error handling, logging, and configuration management.
- The tests are basic and may not cover all edge cases.

## License

This project is licensed under the [MIT License](LICENSE).

