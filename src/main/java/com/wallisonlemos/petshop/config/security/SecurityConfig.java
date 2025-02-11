    package com.wallisonlemos.petshop.config.security;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.Customizer;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
    import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity
    public class SecurityConfig {

        final
        SecurityFilter securityFilter;

        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        final CustomAccessDeniedHandler customAccessDeniedHandler;

        public SecurityConfig(SecurityFilter securityFilter, CustomAuthenticationEntryPoint customAuthenticationEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler) {
            this.securityFilter = securityFilter;
            this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
            this.customAccessDeniedHandler = customAccessDeniedHandler;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
            return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder(){
            return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
            http
                    .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                    .cors(Customizer.withDefaults())
                    .csrf(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint(customAuthenticationEntryPoint)
                            .accessDeniedHandler(customAccessDeniedHandler)
                    )
                    .authorizeHttpRequests(req -> req.requestMatchers("/").permitAll())
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.POST, "/auth/login").permitAll())
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.POST, "/auth/**").hasAnyRole("ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.PUT, "/auth/**").hasAnyRole("CLIENTE", "ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.DELETE, "/auth/**").hasAnyRole("ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.GET, "/clientes/com-pets").hasAnyRole("ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.POST, "/pets/**").hasAnyRole("ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.PUT, "/pets/**").hasAnyRole("CLIENTE", "ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.DELETE, "pets/**").hasAnyRole("ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.GET, "atendimentos/**").hasAnyRole("CLIENTE", "ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.POST, "atendimentos/**").hasAnyRole("ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.PUT, "atendimentos/**").hasAnyRole("ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.DELETE, "atendimentos/**").hasAnyRole("ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers("/usuarios/**").hasAnyRole("CLIENTE", "ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers("/administradores/**").hasRole("ADMIN"))
                    .authorizeHttpRequests(req -> req.requestMatchers("/h2-console/**").permitAll())
                    .authorizeHttpRequests(req -> req.anyRequest().authenticated())
                    .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }
    }
