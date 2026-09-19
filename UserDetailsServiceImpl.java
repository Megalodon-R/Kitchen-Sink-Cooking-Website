package com.swivel.recipes;

import com.swivel.model.User;
import com.swivel.dao.UserDAO;
import com.swivel.security.JwtService;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserService users;

  public UserDetailsServiceImpl(UserService users) { this.users = users; }

  // loads
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User u = users.findByUsername(username)
                  .orElseThrow(() -> new UsernameNotFoundException(username));

    // Blocks login attempts if deactivated
    Boolean active = users.isActive(username); // null is already handled in case of a missing user
    if (Boolean.FALSE.equals(active)) {
      throw new DisabledException("User is deactivated");
    }
    
    String role = JwtService.clearanceToRole(u.getClearance()); // ROLE_USER and ROLE_ADMIN
    return new org.springframework.security.core.userdetails.User(
        u.getUsername(),
        u.getPasswordHash(),
        List.of(new SimpleGrantedAuthority(role))
    );
  }
  
  
  
}
