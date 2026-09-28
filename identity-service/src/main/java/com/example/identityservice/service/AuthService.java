package com.example.identityservice.service;

import com.example.identityservice.dto.request.LoginRequest;
import com.example.identityservice.dto.response.AuthResponse;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.repository.RefreshTokenRepository;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Tìm user trong Database
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        // 2. So sánh mật khẩu đầu vào với mật khẩu băm BCrypt trong Database
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        // 3. Sinh Access Token (HS256, 15 phút, chứa sub, role, jti)
        String accessToken = jwtProvider.generateAccessToken(user.getUsername(), user.getRole());

        // 4. Sinh Refresh Token (Chuỗi UUID ngẫu nhiên, sống 7 ngày)
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .username(user.getUsername())
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        // 5. Lưu Refresh Token vào bảng refresh_tokens
        refreshTokenRepository.save(refreshToken);

        // 6. Trả về cặp Access Token và Refresh Token
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
}