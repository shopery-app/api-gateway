package az.shopery.api_gateway.handler;

import az.shopery.api_gateway.utils.common.ErrorResponseWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityExceptionHandler implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        log.debug("Authentication failed: {}", ex.getMessage());
        return errorResponseWriter.write(exchange, HttpStatus.UNAUTHORIZED, "Invalid or missing access token!");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
        log.debug("Access denied: {}", ex.getMessage());
        return errorResponseWriter.write(exchange, HttpStatus.FORBIDDEN, "Access denied!");
    }
}
