package pvlov;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ClamdClient is a reactive client for interacting with the ClamAV daemon (Clamd).
 * It provides methods to check the availability of the Clamd server and to scan
 * data for potential threats using the ClamAV antivirus engine.
 *
 * <p>This client uses Project Reactor's reactive features to handle
 * asynchronous communication with the Clamd server, allowing for non-blocking
 * operations. It also supports pooling TCP-Connections under the hood.</p>
 */
public class ClamdClient {

    private static final int CHUNK_SIZE = 4096;

    private static final Publisher<byte[]> PING = Mono.just("zPING\0".getBytes(StandardCharsets.US_ASCII));

    private static final Publisher<byte[]> START_OF_INSTREAM = Mono.just("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));

    private static final Publisher<byte[]> END_OF_INSTREAM = Mono.just("\0\0\0\0".getBytes(StandardCharsets.US_ASCII));

    private static final Mono<String> ERROR_CLAMD_NOT_RUNNING = Mono.error(new IllegalStateException("ClamAV Server did not respond."));

    private final TcpClient tcpClient;

    /**
     * Constructs a new ClamdClient instance with the specified host, port, maximum
     * connections, and pending acquire timeout. To ensure the client is ready for use,
     * the warmup process is initiated immediately after construction and blocks until
     * the client is fully initialized.
     *
     * @param host                  The hostname or IP address of the ClamAV daemon.
     * @param port                  The port number on which the ClamAV daemon is listening.
     * @param maxConnections        The maximum number of connections to the ClamAV daemon.
     * @param pendingAcquireTimeout The timeout duration for acquiring a connection from the pool.
     */
    public ClamdClient(final String host, final int port, final int maxConnections, final Duration pendingAcquireTimeout) {

        final String poolName = "clamd::tcp::pool::" + UUID.randomUUID();

        final ConnectionProvider pool = ConnectionProvider.builder(poolName)
                .maxConnections(maxConnections)
                .pendingAcquireMaxCount(maxConnections * 2)
                .pendingAcquireTimeout(pendingAcquireTimeout)
                .build();

        this.tcpClient = TcpClient.create(pool)
                .host(host)
                .port(port)
                .secure();

        tcpClient.warmup().block();
    }

    /**
     * Translates the response from the ClamAV server into a {@link Scan} result.
     *
     * @param response The response string from the ClamAV server.
     * @return A {@link Scan} object representing the result of the scan.
     */
    private static Scan translateResponse(final String response) {
        if (response.startsWith("INSTREAM size limit exceeded.")) {
            return Scan.sizeExceeded(response);
        }

        if (response.contains("OK") && !response.contains("FOUND")) {
            return Scan.clean();
        }

        return Scan.infected(response);
    }


    /**
     * Checks the availability of the ClamAV daemon by sending a PING command and
     * verifying the response. The server is expected to reply with a "PONG" response
     * if it is operational.
     *
     * <p><strong>Note:</strong> The ClamAV daemon must be running and accessible at
     * the configured host and port for this method to function correctly. If the
     * server is unreachable or an error occurs during the connection, the method
     * will return {@code false}.</p>
     *
     * @return A {@link Mono} emitting {@code true} if the ClamAV daemon responds
     * with "PONG", or {@code false} if it does not respond or an error occurs.
     */
    public Mono<Boolean> isAlive() {
        return tcpClient
                .connect()
                .flatMap(connection -> connection.outbound()
                        .sendByteArray(PING)
                        .then()
                        .thenMany(connection.inbound().receive().asString(StandardCharsets.US_ASCII))
                        .collect(Collectors.joining())
                ).map(response -> response.contains("PONG"))
                .onErrorReturn(false);
    }

    /**
     * Scans the provided data for potential threats using the ClamAV daemon.
     *
     * <p>This method sends the data to the ClamAV server using the INSTREAM command,
     * which allows scanning of data streams without writing them to disk.
     * The server's response is then translated into a {@link Scan}
     * result, indicating whether the data is clean, infected, or if the size limit
     * was exceeded.</p>
     *
     *
     * <p><strong>Note:</strong> The ClamAV daemon must be running and accessible at
     * the configured host and port for this method to function correctly. If the daemon
     * is not running or it decided to not return a response, the returned Mono will emit
     * an {@link IllegalStateException}</p>
     *
     * @param data The byte array representing the data to be scanned. This can be
     *             any binary content, such as files or streams, that needs to be
     *             checked for threats.
     * @return A {@link Mono} emitting the {@link Scan} result, which can be one of
     * the following:
     * <ul>
     *     <li>{@link Scan.Clean} - If the data is free of threats.</li>
     *     <li>{@link Scan.Infected} - If the data contains a threat, with
     *         details provided in the response.</li>
     *     <li>{@link Scan.SizeExceeded} - If the data exceeds the size limit
     *         allowed by the ClamAV daemon.</li>
     * </ul>
     * If an error occurs during the scan, the {@link Mono} will emit an error.
     */
    public Mono<Scan> scan(final byte[] data) {

        // Since an empty byte array will result in the datastream
        // "zINSTREAM\0 \0\0\0\0 \0\0\0\0" being sent to the server
        // we need to handle this case separately.
        if (data.length == 0) {
            // An empty byte array is considered clean.
            return Mono.just(Scan.clean());
        }

        final var dataStream = Flux.concat(
                START_OF_INSTREAM,
                splitIntoPackages(data),
                END_OF_INSTREAM
        );

        return tcpClient.connect()
                .flatMap(conn ->
                        conn.outbound()
                                .sendByteArray(dataStream)
                                .then()
                                .then(
                                        conn.inbound()
                                                .receive()
                                                // We have to make sure to set the StandardCharsets here
                                                // since we don't expect UTF-8 Data from the server.
                                                // This saves a lot of performance since we skip the validation of UTF-8.
                                                .asString(StandardCharsets.US_ASCII)
                                                .collect(Collectors.joining(System.lineSeparator()))
                                                // the funny part here is, that if the clamav daemon is not running
                                                // it will just return an empty Mono, which in our case is an error.
                                                .switchIfEmpty(ERROR_CLAMD_NOT_RUNNING)
                                                .map(ClamdClient::translateResponse)
                                )
                                // clean up connection no matter what
                                .doOnTerminate(conn::dispose)
                );
    }

    /**
     * Splits the given byte array into smaller chunks suitable for the INSTREAM command
     * used by the ClamAV daemon. Each chunk is prefixed with 4 bytes in network order
     * indicating the size of the chunk, as required by the ClamAV protocol.
     *
     * <p>The method ensures that the data is divided into chunks of a maximum size
     * defined by {@code CHUNK_SIZE}. If the data length is not a multiple of {@code CHUNK_SIZE},
     * the last chunk will contain the remaining bytes.</p>
     *
     * @param data The byte array to be split into INSTREAM-compatible packages.
     * @return A {@link Flux} emitting byte arrays, where each array represents a chunk
     * prefixed with its size in 4 bytes.
     */
    private Flux<byte[]> splitIntoPackages(final byte[] data) {
        return Flux.range(0, (data.length + CHUNK_SIZE - 1) / CHUNK_SIZE)
                .map(i -> {
                    final int start = i * CHUNK_SIZE;
                    final int end = Math.min((i + 1) * CHUNK_SIZE, data.length);
                    final byte[] chunk = Arrays.copyOfRange(data, start, end);

                    final ByteBuffer buffer = ByteBuffer.allocate(4 + chunk.length)
                            .putInt(chunk.length)
                            .put(chunk);
                    return buffer.array();
                });
    }
}
