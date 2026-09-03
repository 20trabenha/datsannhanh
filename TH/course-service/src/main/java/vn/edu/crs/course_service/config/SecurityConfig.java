package vn.edu.crs.course_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final GatewayRoleAuthenticationFilter gatewayRoleAuthenticationFilter;

    public SecurityConfig(GatewayRoleAuthenticationFilter gatewayRoleAuthenticationFilter) {
        this.gatewayRoleAuthenticationFilter = gatewayRoleAuthenticationFilter;
    }

    // =========================
    // Password Encoder
    // =========================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================
    // Security Configuration
    // =========================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // Không sử dụng CSRF cho REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Cho phép CORS từ Frontend React
                .cors(cors -> {})

                .addFilterBefore(gatewayRoleAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Cho phép H2 Console chạy trong iframe
                .headers(headers ->
                        headers.frameOptions(
                                HeadersConfigurer.FrameOptionsConfig::disable
                        )
                )

                // =========================
                // Phân quyền API
                // =========================
                .authorizeHttpRequests(auth -> auth

                        // API đăng nhập / đăng ký
                        .requestMatchers("/api/auth/**").permitAll()

                        // Anyone may view the catalogue; only administrators can change it.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/courses/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/courses/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/courses/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/courses/**").hasRole("ADMIN")

                        // H2 Console
                        .requestMatchers("/h2-console/**").permitAll()

                        // Các request OPTIONS của CORS
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // Những API khác bắt buộc đăng nhập
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    // =========================
    // CORS Configuration
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Frontend React/Vite
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        // Các HTTP method được phép
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        // Cho phép tất cả request headers
        configuration.setAllowedHeaders(
                List.of("*")
        );

        // Cho phép gửi Cookie / Authorization
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
