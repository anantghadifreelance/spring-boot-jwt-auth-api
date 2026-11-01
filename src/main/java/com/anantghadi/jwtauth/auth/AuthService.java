package com.anantghadi.jwtauth.auth;

import com.anantghadi.jwtauth.auth.AuthDtos.AuthResponse;
import com.anantghadi.jwtauth.auth.AuthDtos.LoginRequest;
import com.anantghadi.jwtauth.auth.AuthDtos.RegisterRequest;
import com.anantghadi.jwtauth.exception.ApiException;
import com.anantghadi.jwtauth.security.JwtService;
import com.anantghadi.jwtauth.user.Role;
import com.anantghadi.jwtauth.user.User;
import com.anantghadi.jwtauth.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long refreshTokenExpirationMs;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = userRepository.save(new User(
                request.name(), request.email(), passwordEncoder.encode(request.password()), Role.USER));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return issueTokens(user);
    }

    /** Rotates the refresh token: the old one is revoked and a new pair is issued. */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .filter(RefreshToken::isUsable)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired"));
        stored.revoke();
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            refreshTokenRepository.findAllByUserAndRevokedFalse(token.getUser()).forEach(RefreshToken::revoke);
        });
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenRepository.save(new RefreshToken(
                UUID.randomUUID().toString(), user, Instant.now().plusMillis(refreshTokenExpirationMs)));
        return new AuthResponse(accessToken, refreshToken.getToken(), "Bearer",
                jwtService.getAccessTokenExpirationMs() / 1000);
    }
}
