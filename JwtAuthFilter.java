package com.swivel.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.http.HttpHeaders;

import java.time.Duration;
import java.util.List;

// the ultimate authentication filter for our security. This navigates paths
// and controls the activeness of guard dogging for our website's areas and permissions
// especially as you flip between pages.

public class JwtAuthFilter extends org.springframework.web.filter.OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
  private final JwtService jwt;

  public JwtAuthFilter(JwtService jwt) { this.jwt = jwt; }

  @Override
  protected void doFilterInternal(
      jakarta.servlet.http.HttpServletRequest request,
      jakarta.servlet.http.HttpServletResponse response,
      jakarta.servlet.FilterChain chain
  ) throws jakarta.servlet.ServletException, java.io.IOException {

    final String path = request.getRequestURI();
    final String method = request.getMethod();

    // Bypass only preflight and /auth/**
    if ("OPTIONS".equalsIgnoreCase(method) || path.startsWith("/auth/")) {
      log.debug("JWT-FILTER bypass {} {}", method, path);
      chain.doFilter(request, response);
      return;
    }

    // continues if already authenticated
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      log.debug("JWT-FILTER already authenticated -> continue {} {}", method, path);
      chain.doFilter(request, response);
      return;
    }

    // Try ACCESS_TOKEN first
    String access = getCookie(request, "ACCESS_TOKEN");
    log.debug("JWT-FILTER path={} method={} cookiePresent={}", path, method, access != null);

    boolean authenticated = false;
    if (access != null && jwt.isValid(access)) {
      authenticated = authenticateFromToken(access, request, "ACCESS");
    } else {
      if (access != null) log.debug("JWT-FILTER ACCESS token invalid for {} {}", method, path);
      else log.debug("JWT-FILTER no ACCESS_TOKEN cookie for {} {}", method, path);
    }

    // silent refresh
    if (!authenticated) {
      String refresh = getCookie(request, "REFRESH_TOKEN");
      if (refresh != null && jwt.isValid(refresh)) {
        boolean ok = authenticateFromToken(refresh, request, "REFRESH");
        if (ok) {
          // Try to make fresh ACCESS cookie next requests
          try {
            long userId = -1L;
            try {
              userId = jwt.getUserId(refresh);
            } catch (Throwable ignore) {
              try {
                String sub = jwt.getSubject(refresh);
                if (sub != null) userId = Long.parseLong(sub);
              } catch (Throwable ignored) { /* leave userId = -1 */ }
            }

            String username = jwt.getUsername(refresh);
            int clr = jwt.getClearance(refresh);

            // for issueAccessToken()
            String newAccess = jwt.issueAccessToken(userId, username, clr, 15 * 60);

            var cookie = jwt.httpOnlyCookie("ACCESS_TOKEN", newAccess, Duration.ofMinutes(15))
                            .mutate().sameSite("Lax").secure(false).path("/").build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            log.debug("JWT-FILTER refreshed ACCESS via REFRESH for user={}", username);
          } catch (Throwable t) {
            log.debug("JWT-FILTER refresh-auth applied (no new ACCESS cookie): {}", t.toString());
          }
          authenticated = true;
        } else {
          log.debug("JWT-FILTER REFRESH present but could not authenticate");
        }
      } else if (refresh != null) {
        log.debug("JWT-FILTER REFRESH token invalid");
      } else {
        log.debug("JWT-FILTER no REFRESH_TOKEN cookie");
      }
    }

    chain.doFilter(request, response);
  }

  // Routine to set SecurityContext from a token (access or refresh).
  private boolean authenticateFromToken(String token, jakarta.servlet.http.HttpServletRequest request, String kind) {
    try {
      String username = jwt.getUsername(token);
      int clr = jwt.getClearance(token);
      String role = JwtService.clearanceToRole(clr);

      java.util.List<org.springframework.security.core.GrantedAuthority> auths =
    		  new java.util.ArrayList<>();
    		auths.add(new SimpleGrantedAuthority(role));
    		if ("ROLE_ADMIN".equals(role)) {
    		  // also grant bare authority to satisfy any hasRole/hasAuthority differences
    		  auths.add(new SimpleGrantedAuthority("ADMIN"));
    		} else if ("ROLE_MODERATOR".equals(role)) {
    		  auths.add(new SimpleGrantedAuthority("MODERATOR"));
    		} else if ("ROLE_USER".equals(role)) {
    		  auths.add(new SimpleGrantedAuthority("USER"));
    		}

    		var auth = new UsernamePasswordAuthenticationToken(username, null, auths);
    		auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    		SecurityContextHolder.getContext().setAuthentication(auth);
    		
      log.debug("JWT-FILTER authenticated ({}) user={} role={}", kind, username, role);
      return true;
    } catch (Throwable t) {
      log.debug("JWT-FILTER {} token parse/auth failed: {}", kind, t.toString());
      return false;
    }
  }

  private static String getCookie(jakarta.servlet.http.HttpServletRequest req, String name) {
    var cookies = req.getCookies();
    if (cookies == null) return null;
    for (var c : cookies) if (name.equals(c.getName())) return c.getValue();
    return null;
  }
}

