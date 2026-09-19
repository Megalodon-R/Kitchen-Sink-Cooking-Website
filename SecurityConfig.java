package com.swivel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

// This is the keypannel that basically assigns authority for what commands.
// so if you want to be able to restrict the permissions of something to a different level, you do it here.

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

  @Bean
  public JwtAuthFilter jwtAuthFilter(JwtService jwt) {
    return new JwtAuthFilter(jwt);
  }
  
  // order is important or everything blows up
  // Filter chain is establishing what permissions and paths are given to whom. 
  @Order(0)
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(c -> c.configurationSource(req -> {
        var cfg = new org.springframework.web.cors.CorsConfiguration();
        cfg.setAllowedOriginPatterns(java.util.List.of("http://localhost:*", "http://127.0.0.1:*"));
        cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(java.util.List.of("*"));
        cfg.setExposedHeaders(java.util.List.of("Set-Cookie"));
        cfg.setAllowCredentials(true);
        return cfg;
      }))
      .sessionManagement(sm -> sm.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(reg -> reg
        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/error").permitAll() // important for error handling
        .requestMatchers("/auth/**").permitAll()
        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/recipes/**", "/api/tags").permitAll()

        // Admin routes authority, this particular section is for recipes creation/deletion
        .requestMatchers(org.springframework.http.HttpMethod.POST,   "/api/admin/recipes", "/api/admin/recipes/**").hasAuthority("ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/admin/recipes/**").hasAuthority("ROLE_ADMIN")
        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

        // Other authenticated routes
        .requestMatchers("/api/me/**").authenticated()
        .requestMatchers("/api/recipes/*/save").authenticated()
        .requestMatchers("/api/recipes/*/comments").authenticated()
        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/recipes/*/rate").authenticated()

        .anyRequest().authenticated()
      )
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((req, res, e) -> {
        	// some logging points as permissions are hard and we are all blind because login is invisible
        	// particularly on what permissions they're given.
          org.slf4j.LoggerFactory.getLogger(SecurityConfig.class)
            .warn("AUTH ENTRY: {} {} user={} auths={}",
                  req.getMethod(), req.getRequestURI(),
                  req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "anonymous",
                  org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null
                    ? org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    : "null");
          res.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
        })
        .accessDeniedHandler((req, res, e) -> {
        	// tells us where access is denied. this was a big help when failing for the hundredth time at
        	// adding a recipe. turns out the 403 was not caused by permissions, but useful nontheless.
          org.slf4j.LoggerFactory.getLogger(SecurityConfig.class)
            .warn("ACCESS DENIED: {} {} user={} auths={}",
                  req.getMethod(), req.getRequestURI(),
                  req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "anonymous",
                  org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null
                    ? org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    : "null");
          res.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
        })
      )
      .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }


  @Bean
  public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
  }
}
