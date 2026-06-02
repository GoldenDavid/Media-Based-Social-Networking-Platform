package com.socialnetwork.controller.auth;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.socialnetwork.dto.AuthResponse;
import com.socialnetwork.dto.LoginRequest;
import com.socialnetwork.dto.RegisterRequest;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.model.User;
import com.socialnetwork.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
      );
      
      SecurityContextHolder.getContext().setAuthentication(authentication);

      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      log.info("User {} logged in successfully.", userPrincipal.getUsername());
      
      return ResponseEntity.ok(new AuthResponse(userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getName()));
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
      if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
          return ResponseEntity.badRequest().body("Username already exists");
      }

      User user = new User();
      user.setId(UUID.randomUUID());
      user.setName(registerRequest.getName());
      user.setUsername(registerRequest.getUsername());
      user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
      user.setProvider("local");
      user.setEnabled(true);
      user.setAccountNonLocked(true);
      user.setAccountNonExpired(true);
      user.setCredentialsNonExpired(true);
      
      userRepository.save(user);
      log.info("User {} registered successfully.", user.getUsername());
      
      return ResponseEntity.ok("User registered successfully");
  }

  @GetMapping("/inspect")
  public ResponseEntity<AuthResponse> inspect(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity.status(401).build();
    }
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    log.info(String.format("authentication %s", userPrincipal.getId()));
    return ResponseEntity.ok().body(new AuthResponse(userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getName()));
  }

  @GetMapping("/check-has-admin-role")
  @PreAuthorize("hasRole('ADMIN')")
  public Object sayHello(Authentication authentication) {
    return ResponseEntity.ok().body("has admin role");
  }

  @GetMapping("/check-has-user-role")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public Object getPrincipal(Authentication authentication) {
    return ResponseEntity.ok().body("has user role");
  }
}
