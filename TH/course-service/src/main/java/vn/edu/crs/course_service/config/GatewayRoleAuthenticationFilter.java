package vn.edu.crs.course_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Accepts the role header added by the API Gateway after JWT validation. */
@Component
public class GatewayRoleAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String username = request.getHeader("X-User-Name");
        String role = request.getHeader("X-User-Role");

        if (username != null && role != null && !role.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authority = new SimpleGrantedAuthority("ROLE_" + role);
            var authentication = new UsernamePasswordAuthenticationToken(username, null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
