package az.shopery.api_gateway.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter, Ordered {

    public static final String ATTR_USER_EMAIL = "X-User-Email";
    public static final String ATTR_USER_ROLES = "X-User-Roles";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/**",
            "/api/v1/shops/**",
            "/api/v1/products/**",
            "/api/v1/blogs/**",
            "/api/v1/dropdowns/**"
    );

    private static final List<String> ADMIN_PATHS = List.of(
            "/api/v1/admins/**"
    );

    private static final List<String> USER_PATHS = List.of(
            "/api/v1/users/me/**"
    );

    private final GatewayJwtService gatewayJwtService;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (isPublic(path)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (Objects.isNull(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            sendError(response, HttpStatus.UNAUTHORIZED, "Missing or Invalid Authorization Header!");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            if (!gatewayJwtService.isTokenValid(token)) {
                sendError(response, HttpStatus.UNAUTHORIZED, "Invalid Token!");
                return;
            }

            String email = gatewayJwtService.extractUsername(token);
            List<String> authorities = gatewayJwtService.extractAuthorities(token);

            if (isAdminPath(path) && !authorities.contains("ADMIN")) {
                sendError(response, HttpStatus.FORBIDDEN, "ADMIN access required!");
                return;
            }

            if (isUserPath(path) && !authorities.contains("USER")) {
                sendError(response, HttpStatus.FORBIDDEN, "USER access required!");
                return;
            }

            request.setAttribute(ATTR_USER_EMAIL, email);
            request.setAttribute(ATTR_USER_ROLES, String.join(",", authorities));

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT processing failed for path {}: {}", path, e.getMessage());
            sendError(response, HttpStatus.UNAUTHORIZED, "Invalid Token!");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
    }

    private boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
    }

    private boolean isUserPath(String path) {
        return USER_PATHS.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\": \"%s\", \"status\": %d}".formatted(message, status.value()));
    }
}
