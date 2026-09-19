package com.swivel.recipes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

// the cookie jar ;)

public class CookieUtil {
  public static String read(HttpServletRequest req, String name) {
    if (req.getCookies() == null) return null;
    for (Cookie c : req.getCookies()) if (name.equals(c.getName())) return c.getValue();
    return null;
  }
}
