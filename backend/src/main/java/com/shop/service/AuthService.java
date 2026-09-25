package com.shop.service;

import com.shop.dto.AuthDtos.*;
import com.shop.model.Cart;
import com.shop.model.Role;
import com.shop.model.User;
import com.shop.repository.CartRepository;
import com.shop.repository.UserRepository;
import com.shop.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.USER);              // always USER, never chosen by the client
        userRepository.save(user);

        Cart cart = new Cart();               // every user gets an empty cart
        cart.setUser(user);
        cartRepository.save(cart);

        return toResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        User user = userRepository.findByEmail(email).orElseThrow();
        return toResponse(user);
    }

    private AuthResponse toResponse(User u) {
        String token = jwtService.generateToken(u.getEmail(), u.getRole());
        return new AuthResponse(token, "Bearer", u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
