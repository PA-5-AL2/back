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

/**
 * The type Security config.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    /**
     * Password encoder password encoder.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager authentication manager.
     *
     * @param http            the http
     * @param passwordEncoder the password encoder
     * @return the authentication manager
     * @throws Exception the exception
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception{
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    /**
     * Security filter chain security filter chain.
     *
     * @param http the http
     * @return the security filter chain
     * @throws Exception the exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/api/auth/login").permitAll()
                                .requestMatchers("/api/auth/register").permitAll()
                                .requestMatchers("/api/emails/**").permitAll()
                                .requestMatchers("/api/test/**").permitAll()
                                .requestMatchers("/api/client-requests/submit").permitAll()
                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/api/password-reset/request").permitAll()
                                .requestMatchers("/api/password-reset/changed-notification").permitAll()
                                .requestMatchers("/api/password-reset/status").permitAll()
                                .requestMatchers("/api/employee-access/**").permitAll()
                                .requestMatchers("/api/customers").permitAll()
                                .requestMatchers(HttpMethod.GET, "/").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                // Endpoints Swagger/OpenAPI publics
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers("/swagger-resources/**").permitAll()
                                .requestMatchers("/webjars/**").permitAll()

                                .requestMatchers(HttpMethod.PUT, "/api/users/clients/*/password").hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/admins/*/password").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/admin/clients/*/password").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/admin/admins/*/password").hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/users/clients/*/access-code").hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/clients/*/access-code/regenerate").hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/users/clients/*/verify-access-code").hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/clients/*/access-code/custom").hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers("/api/promotions/**").permitAll()
                                .requestMatchers("/api/deferred-payments/**").permitAll()
                                .requestMatchers("/api/customers/**").permitAll()

                                .requestMatchers("/api/users/**").hasRole("ADMIN")

                                .anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(customUserDetailsService,jwtUtils), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}