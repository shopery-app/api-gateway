package az.shopery.api_gateway.configuration;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import az.shopery.api_gateway.security.JwtAuthFilter;
import java.net.URI;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class RouterConfig {

    @Value("${shopery.base-url}")
    private String shoperyBaseUrl;

    @Bean
    public RouterFunction<ServerResponse> authRoutes() {
        return route("shopery-auth")
                .route(req -> req.path().startsWith("/api/v1/auth"), http())
                .before(uri(URI.create(shoperyBaseUrl)))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> shopRoutes() {
        return route("shopery-shops")
                .route(req -> req.path().startsWith("/api/v1/shops"), http())
                .before(uri(URI.create(shoperyBaseUrl)))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productRoutes() {
        return route("shopery-products")
                .route(req -> req.path().startsWith("/api/v1/products"), http())
                .before(uri(URI.create(shoperyBaseUrl)))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> blogRoutes() {
        return route("shopery-blogs")
                .route(req -> req.path().startsWith("/api/v1/blogs"), http())
                .before(uri(URI.create(shoperyBaseUrl)))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> dropdownRoutes() {
        return route("shopery-dropdowns")
                .route(req -> req.path().startsWith("/api/v1/dropdowns"), http())
                .before(uri(URI.create(shoperyBaseUrl)))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> adminRoutes() {
        return route("shopery-admin")
                .route(req -> req.path().startsWith("/api/v1/admins"), http())
                .before(uri(URI.create(shoperyBaseUrl)))
                .before(this::injectIdentityHeaders)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userMeRoutes() {
        return route("shopery-user-me")
                .route(req -> req.path().startsWith("/api/v1/users/me"), http())
                .before(uri(URI.create(shoperyBaseUrl)))
                .before(this::injectIdentityHeaders)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> otherApiRoutes() {
        return route("shopery-other-api")
                .route(req -> req.path().startsWith("/api/v1"), http())
                .before(uri(URI.create(shoperyBaseUrl)))
                .before(this::injectIdentityHeaders)
                .build();
    }

    private ServerRequest injectIdentityHeaders(ServerRequest serverRequest) {
        String email = (String) serverRequest.servletRequest().getAttribute(JwtAuthFilter.ATTR_USER_EMAIL);
        String roles = (String) serverRequest.servletRequest().getAttribute(JwtAuthFilter.ATTR_USER_ROLES);

        ServerRequest.Builder builder = ServerRequest.from(serverRequest);
        if (Objects.nonNull(email)) {
            builder.header(JwtAuthFilter.ATTR_USER_EMAIL, email);
        }
        if (Objects.nonNull(roles)) {
            builder.header(JwtAuthFilter.ATTR_USER_ROLES, roles);
        }
        return builder.build();
    }
}
