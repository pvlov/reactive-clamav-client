package pvlov;

import org.openjdk.jmh.annotations.*;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ClamdClientBenchmark {

    private ClamdClient clamdClient;
    private byte[] cleanData;

    @Setup(Level.Trial)
    public void setup() {
        clamdClient = new ClamdClient("localhost", 3310, 4, Duration.ofSeconds(5));

        // check with 5mb of clean data
        cleanData = new byte[5 * 1024 * 1024];
        final Random random = new Random(1337);
        random.nextBytes(cleanData);
    }

    @Benchmark
    public void benchmarkCleanScan() {
        clamdClient.scan(cleanData).subscribe().dispose();
    }
}