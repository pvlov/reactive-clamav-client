package pvlov;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.Duration;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class ApplicationConfig {

    @Bean
    public RouterFunction<ServerResponse> routes (ApplicationController controller) {
        return RouterFunctions.route()
                .GET("/check", accept(MediaType.APPLICATION_OCTET_STREAM).and(contentType(MediaType.APPLICATION_JSON)), controller::check)
                .build();
    }

    @Bean
    public ClamdClient clamdClient() {
        return new ClamdClient("localhost", 3310, 4, Duration.ofSeconds(5));
    }
}
