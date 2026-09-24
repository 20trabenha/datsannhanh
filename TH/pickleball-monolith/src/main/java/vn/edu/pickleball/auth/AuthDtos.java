package vn.edu.pickleball.auth;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(String username, String password, String phoneNumber) {}
    public record LoginRequest(String username, String password) {}
    public record ChangePasswordRequest(String currentPassword, String newPassword) {}
    public record ResetPasswordRequest(String username, String resetCode, String newPassword) {}
    public record CustomerResponse(Long id, String username, String phoneNumber) {}
    public record ResetCodeResponse(String resetCode, String expiresAt) {}
    public record AuthResponse(Long userId, String token, String username, Role role) {}
}
