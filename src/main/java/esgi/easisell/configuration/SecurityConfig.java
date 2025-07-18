package esgi.easisell.configuration;

import esgi.easisell.filter.JwtFilter;
import esgi.easisell.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception{
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    // ✅ AJOUT: Configuration CORS pour résoudre l'erreur 403
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Utiliser allowedOriginPatterns au lieu de allowedOrigins pour éviter l'erreur
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // Pour la production, utilisez plutôt des origines spécifiques :
        // configuration.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:3000",
        //     "http://localhost:5000",
        //     "https://votre-domaine.com"
        // ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                // === ENDPOINTS PUBLICS (SANS AUTHENTIFICATION) ===
                                .requestMatchers(HttpMethod.GET, "/").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                // Auth endpoints
                                .requestMatchers("/api/auth/login").permitAll()
                                .requestMatchers("/api/auth/register").permitAll()
                                .requestMatchers("/api/password-reset/**").permitAll()

                                // Test endpoints
                                .requestMatchers("/api/test/**").permitAll()

                                // Public utilities
                                .requestMatchers("/api/emails/**").permitAll()
                                .requestMatchers("/api/client-requests/submit").permitAll()
                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/api/employee-access/**").permitAll()

                                .requestMatchers("/api/deferred-payments/**").permitAll()
                                .requestMatchers("/api/customers/**").permitAll()
                                .requestMatchers("/api/promotions/**").permitAll()

                                // Swagger/OpenAPI - Publics en développement
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers("/swagger-resources/**").permitAll()
                                .requestMatchers("/webjars/**").permitAll()

                                // === ENDPOINTS AVEC AUTHENTIFICATION ===

                                // Gestion des mots de passe utilisateurs
                                .requestMatchers(HttpMethod.PUT, "/api/users/clients/*/password").hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/admins/*/password").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/admin/clients/*/password").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/admin/admins/*/password").hasRole("ADMIN")

                                // Gestion des codes d'accès
                                .requestMatchers(HttpMethod.GET, "/api/users/clients/*/access-code").hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/clients/*/access-code/regenerate").hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/users/clients/*/verify-access-code").hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/clients/*/access-code/custom").hasAnyRole("CLIENT", "ADMIN")

                                .requestMatchers("/api/sales/**").hasAnyRole("CLIENT", "ADMIN")

                                .requestMatchers("/api/products/**").hasAnyRole("CLIENT", "ADMIN")

                                .requestMatchers("/api/categories/**").hasAnyRole("CLIENT", "ADMIN")

                                // Admin users management
                                .requestMatchers("/api/users/**").hasRole("ADMIN")

                                // Tout le reste nécessite une authentification
                                .anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(customUserDetailsService,jwtUtils), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}