package com.swivel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.Map;

// supplies the main commands for 

@Service
public class JwtService {
	private static final Logger log = LoggerFactory.getLogger(JwtService.class);
	private final SecretKey signingKey;
	private final JwtParser parser;

	// takes your secret key from your password and unhashes
	public JwtService(@Value("${jwt.secret:}") String secret) {
	    String s = (secret == null) ? "" : secret.replaceAll("\\s+", "");
	    SecretKey key;
	    if (s.isEmpty()) {
	      byte[] tmp = new byte[64];
	      new SecureRandom().nextBytes(tmp);
	      key = Keys.hmacShaKeyFor(tmp);
	      log.warn("JwtService: using DEV random key (jwt.secret missing); DO NOT use in production.");
	    } else {
	      try {
	        byte[] decoded = Base64.getDecoder().decode(s);
	        if (decoded.length < 32) {
	          // 256-bit
	          throw new IllegalArgumentException("jwt.secret too short after Base64 decode");
	        }
	        key = Keys.hmacShaKeyFor(decoded);
	        log.info("JwtService: using application jwt.secret ({} bytes)", decoded.length);
	      } catch (IllegalArgumentException ex) {
	        byte[] tmp = new byte[64];
	        new SecureRandom().nextBytes(tmp);
	        key = Keys.hmacShaKeyFor(tmp);
	        log.warn("JwtService: invalid jwt.secret; using DEV random key. {}", ex.getMessage());
	      }
	    }
	    this.signingKey = key;
	    this.parser = Jwts.parserBuilder().setSigningKey(this.signingKey).build();
	  }

	// translator between number and word for authority
  public static List<SimpleGrantedAuthority> clearanceToAuthorities(int c) {
	  return switch (c) {
	  	case 2 -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
	  	case 1 -> List.of(new SimpleGrantedAuthority("ROLE_MODERATOR"));
	  	default -> List.of(new SimpleGrantedAuthority("ROLE_USER"));
	  };
  }
  
  // Applies clearance to a user
  public org.springframework.security.core.Authentication buildAuthentication(String token) {
	  String username = getUsername(token);
	  int c = getClearance(token);
	  String role = clearanceToRole(c);
	  var auths = java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));
	  return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(username, null, auths);
	}
  
  public Authentication buildAuthentication(String username, int clearance) {
	  var auths = JwtService.clearanceToAuthorities(clearance);
	  return new UsernamePasswordAuthenticationToken(username, null, auths);
	}
  
  // SECTION: claims helpers
  private Claims claims(String token) {
    return parser.parseClaimsJws(token).getBody();
  }

  public String getSubject(String token) {
    return claims(token).getSubject();
  }

  public long getUserId(String token) {
    String sub = getSubject(token);
    try {
      return Long.parseLong(sub);
    } catch (NumberFormatException nfe) {
      Object uid = claims(token).get("uid");
      if (uid instanceof Number) return ((Number) uid).longValue();
      throw nfe;
    }
  }

  public String getUsername(String token) {
    Object u = claims(token).get("u");
    return (u == null) ? null : u.toString();
  }

  public int getClearance(String token) {
    Object c = claims(token).get("c");
    if (c instanceof Number) return ((Number) c).intValue();
    if (c != null) {
      try { return Integer.parseInt(c.toString()); } catch (Exception ignore) {}
    }
    return 0;
  }

  // SECTION: Validation
  public boolean isValid(String token) {
    try {
      claims(token); // throws on invalid/expired/signature
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  // Alias for isValid
  public boolean isValidRefresh(String token) {
    return isValid(token);
  }

  // SECTION: ISSUING
  // Issues a short-lived ACCESS token
  public String issueAccessToken(long userId, String username, int clearance, int expiresSeconds) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + expiresSeconds * 1000L);
    return Jwts.builder()
      .setSubject(Long.toString(userId))// "sub"
      .claim("u", username)// username
      .claim("c", clearance)// clearance
      .setIssuedAt(now)
      .setExpiration(exp)
      .signWith(signingKey, SignatureAlgorithm.HS256)
      .compact();
  }

  // SECTION: Cookies
  public ResponseCookie httpOnlyCookie(String name, String val, Duration maxAge) {
    return ResponseCookie.from(name, val)
      .httpOnly(true)
      .secure(false)     // keep false on http://localhost; set true behind HTTPS
      .sameSite("Lax")
      .path("/")
      .maxAge(maxAge)
      .build();
  }

  // SECTION: ROLE MAPPING
  public static String clearanceToRole(int clr) {
    return switch (clr) {
      case 2 -> "ROLE_ADMIN";
      case 1 -> "ROLE_MODERATOR";
      default -> "ROLE_USER";
    };
  }
}

