package vn.edu.pickleball.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.pickleball.auth.AuthDtos.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LoginAttemptService attempts;
    private final SecureRandom random = new SecureRandom();
    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt, LoginAttemptService attempts) {
        this.users = users; this.encoder = encoder; this.jwt = jwt; this.attempts = attempts;
    }
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request == null || request.username() == null || request.username().isBlank())
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        if (!request.username().matches("[A-Za-z0-9]+"))
            throw new IllegalArgumentException("Tên đăng nhập không được chứa ký tự đặc biệt.");
        if (request.password() == null || request.password().isBlank())
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        if (request.phoneNumber() == null || !request.phoneNumber().matches("[0-9]{10}"))
            throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số, vui lòng nhập lại.");
        if (users.existsByUsername(request.username()))
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        User user = users.save(new User(request.username(), encoder.encode(request.password()), request.phoneNumber(), Role.CUSTOMER));
        return response(user);
    }
    public AuthResponse login(LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null)
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không chính xác.");
        User user = users.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không chính xác."));
        if (user.isLocked())
            throw new IllegalArgumentException("Đăng nhập thất bại. Vui lòng thử lại sau nếu đã nhập sai nhiều lần.");
        if (!encoder.matches(request.password(), user.getPassword())) {
            attempts.fail(user.getId());
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
        attempts.clear(user.getId());
        return response(user);
    }
    @Transactional
    public void changePassword(User current, ChangePasswordRequest request) {
        if (request == null || request.currentPassword() == null || request.newPassword() == null)
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại và mật khẩu mới.");
        if (request.newPassword().length() < 8)
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 8 ký tự.");
        User user = users.findLockedById(current.getId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản."));
        if (!encoder.matches(request.currentPassword(), user.getPassword()))
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        if (encoder.matches(request.newPassword(), user.getPassword()))
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        user.changePassword(encoder.encode(request.newPassword()));
    }
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request == null || request.username() == null || request.resetCode() == null ||
                request.newPassword() == null || request.newPassword().length() < 8)
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập, mã đặt lại và mật khẩu mới ít nhất 8 ký tự.");
        User found = users.findByUsername(request.username()).orElse(null);
        if (found == null) throw new IllegalArgumentException("Mã đặt lại không hợp lệ hoặc đã hết hạn.");
        User user = users.findLockedById(found.getId()).orElseThrow();
        if (user.getResetCodeHash() == null || user.getResetCodeExpiresAt() == null ||
                !user.getResetCodeExpiresAt().isAfter(LocalDateTime.now()) ||
                !encoder.matches(request.resetCode(), user.getResetCodeHash()))
            throw new IllegalArgumentException("Mã đặt lại không hợp lệ hoặc đã hết hạn.");
        user.changePassword(encoder.encode(request.newPassword()));
    }
    @Transactional(readOnly = true)
    public List<CustomerResponse> customers() {
        return users.findByRoleOrderByUsernameAsc(Role.CUSTOMER).stream()
                .map(u -> new CustomerResponse(u.getId(), u.getUsername(), u.getPhoneNumber())).toList();
    }
    @Transactional
    public ResetCodeResponse issueResetCode(Long id) {
        User user = users.findLockedById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy khách hàng."));
        if (user.getRole() != Role.CUSTOMER) throw new IllegalArgumentException("Chỉ cấp mã cho tài khoản khách hàng.");
        byte[] bytes = new byte[18]; random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        user.issueResetCode(encoder.encode(code), expiresAt);
        return new ResetCodeResponse(code, expiresAt.toString());
    }
    private AuthResponse response(User user) {
        return new AuthResponse(user.getId(), jwt.create(user), user.getUsername(), user.getRole());
    }
}
