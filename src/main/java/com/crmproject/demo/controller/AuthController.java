package com.crmproject.demo.controller;

import com.crmproject.demo.dto.AuthResponse;
import com.crmproject.demo.dto.LoginRequest;
import com.crmproject.demo.dto.UserResponse;
import com.crmproject.demo.model.User;
import com.crmproject.demo.security.JwtService;
import com.crmproject.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        return ResponseEntity.ok(service.cadastrar(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(request.email(), request.senha());

        UserDetails userDetails = (UserDetails) authenticationManager.authenticate(authToken).getPrincipal();

        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // GET /api/auth/me - Dados do usuário autenticado (usado pelo front-end para decisões de UI por role)
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return new UserResponse(user.getEmail(), user.getRole());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Email ou senha inválidos"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email já cadastrado"));
    }

}
