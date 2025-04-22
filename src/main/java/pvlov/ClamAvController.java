package pvlov;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@org.springframework.stereotype.Controller
public class Controller {

    private final ClamdService clamdService;

    private Controller(final ClamdService clamdService) {
        this.clamdService = clamdService;
    }

    public Mono<ServerResponse> check(final ServerRequest request) {
        return null;
    }
}
