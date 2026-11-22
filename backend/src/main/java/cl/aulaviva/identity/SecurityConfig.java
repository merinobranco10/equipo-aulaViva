package cl.aulaviva.identity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad del MVP (ADR 0008): HTTP Basic sobre HTTPS, API stateless y autorizacion por rol
 * con @PreAuthorize. OIDC/Keycloak queda como objetivo de produccion.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http, ProblemSecurityHandlers handlers)
            throws Exception {
        http
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // La API es stateless y se autentica con el header Authorization en cada request
            // (sin cookies de sesion), por lo que CSRF no aplica a /v1/** (ver threat-model).
            .csrf(c -> c.ignoringRequestMatchers("/v1/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/*.html", "/css/**", "/js/**", "/img/**",
                    "/favicon.ico", "/error", "/api/v1/status", "/actuator/health",
                    "/actuator/health/**", "/actuator/info").permitAll()
                // Métricas y estado del circuit breaker: solo coordinación (SLOs del Día 5)
                .requestMatchers("/actuator/prometheus", "/actuator/circuitbreakers")
                    .hasRole("COORDINATOR")
                .anyRequest().authenticated())
            // Cabeceras para la UI estatica (sin scripts ni estilos inline) y hallazgos del DAST.
            .headers(h -> h
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; "
                        + "object-src 'none'; frame-ancestors 'none'; base-uri 'self'; "
                        + "form-action 'self'"))
                .permissionsPolicyHeader(pp -> pp.policy(
                    "camera=(), microphone=(), geolocation=(), payment=(), usb=()"))
                .crossOriginEmbedderPolicy(coep -> coep.policy(
                    org.springframework.security.web.header.writers
                        .CrossOriginEmbedderPolicyHeaderWriter.CrossOriginEmbedderPolicy.REQUIRE_CORP))
                .crossOriginOpenerPolicy(coop -> coop.policy(
                    org.springframework.security.web.header.writers
                        .CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN)))
            .httpBasic(basic -> basic.authenticationEntryPoint(handlers))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(handlers)
                .accessDeniedHandler(handlers));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
