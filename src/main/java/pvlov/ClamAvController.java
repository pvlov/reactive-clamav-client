package pvlov;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;


@Component
public record ClamAvController(ClamdService clamdService) {
    public Mono<ServerResponse> check(final ServerRequest request) {
        return request.bodyToMono(byte[].class)
                .flatMap(clamdService::check)
                .flatMap(scanResult -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("result", scanResult.toString()))
                )
                .onErrorResume(e -> ServerResponse.status(500)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("error", e.getMessage()))
                );
    }
}
