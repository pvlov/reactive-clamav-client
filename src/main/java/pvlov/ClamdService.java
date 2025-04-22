package pvlov;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public record ClamdService(ClamdClient clamdClient) {

    public Mono<Scan> check(final byte[] data) {
        return clamdClient.scan(data);
    }
}
