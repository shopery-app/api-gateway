package az.shopery.api_gateway.handler;

import az.shopery.api_gateway.utils.common.ErrorResponseWriter;
import java.net.ConnectException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            log.error("Unexpected error occurred: ", ex);
        } else {
            log.warn("Request {} failed with {}: {}", exchange.getRequest().getPath(), status.value(), ex.getMessage());
        }
        return errorResponseWriter.write(exchange, status, resolveMessage(status));
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            return Objects.requireNonNullElse(HttpStatus.resolve(responseStatusException.getStatusCode().value()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (ex instanceof ConnectException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(HttpStatus status) {
        return switch (status) {
            case SERVICE_UNAVAILABLE -> "Service is temporarily unavailable!";
            case GATEWAY_TIMEOUT -> "Service did not respond in time!";
            case INTERNAL_SERVER_ERROR -> "An unexpected error occurred";
            default -> status.getReasonPhrase();
        };
    }
}
