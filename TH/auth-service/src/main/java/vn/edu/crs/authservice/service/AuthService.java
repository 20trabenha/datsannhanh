package vn.edu.crs.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.crs.authservice.dto.AuthResponse;
import vn.edu.crs.authservice.dto.LoginRequest;
import vn.edu.crs.authservice.dto.RegisterRequest;
import vn.edu.crs.authservice.entity.Role;
import vn.edu.crs.authservice.entity.User;
import vn.edu.crs.authservice.repository.UserRepository;
import vn.edu.crs.authservice.util.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.STUDENT)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác!");
        }

        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}