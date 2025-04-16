package pvlov;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;

@Testcontainers
public class ClamdClientTest {

    private static final int CLAMD_PORT = 3310;

    // Sadly the official clamav/clamav image is not available for aarch64
    private static final String CLAMAV_IMAGE = "mkodockx/docker-clamav:alpine";


    @Container
    @SuppressWarnings("resource") // we close the container in tearDown()
    private static final GenericContainer<?> clamdContainer = new GenericContainer<>(CLAMAV_IMAGE)
            .withExposedPorts(CLAMD_PORT)
            .withStartupTimeout(Duration.ofMinutes(2));

    private static ClamdClient clamdClient;

    @BeforeAll
    static void setUp() {
        clamdContainer.start();

        final String host = clamdContainer.getHost();
        final int port = clamdContainer.getMappedPort(CLAMD_PORT);

        clamdClient = new ClamdClient(host, port, 4, Duration.ofSeconds(5));
    }

    @AfterAll
    static void tearDown() {
        if (clamdContainer != null && clamdContainer.isRunning()) {
            clamdContainer.stop();
        }
    }

    @Test
    void testIsAlive() {
        StepVerifier.create(clamdClient.isAlive())
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testSimpleCleanScan() {
        final byte[] testData = "Hello, World!".getBytes();

        StepVerifier.create(clamdClient.scan(testData))
                .expectNext(Scan.clean())
                .verifyComplete();
    }

    @Test
    void testSimpleInfectedScan() {
        final byte[] testData = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes();

        StepVerifier.create(clamdClient.scan(testData))
                .expectNextMatches(scan -> scan instanceof Scan.Infected)
                .verifyComplete();
    }
}
