package vn.edu.crs.apigateway.validator;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();

        // The course catalogue is shown before a user signs in, so its read
        // endpoints must not require a JWT.  Write operations remain secured.
        boolean isPublicCourseRead = HttpMethod.GET.equals(request.getMethod())
                && path.startsWith("/api/courses");
        boolean isOpenEndpoint = openApiEndpoints.stream().anyMatch(path::startsWith);

        return !isPublicCourseRead && !isOpenEndpoint;
    };
}
