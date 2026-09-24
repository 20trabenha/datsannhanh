package vn.edu.pickleball.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(length = 10)
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private Integer tokenVersion;
    private String resetCodeHash;
    private LocalDateTime resetCodeExpiresAt;

    protected User() {}
    public User(String username, String password, String phoneNumber, Role role) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
    public Role getRole() { return role; }
    public int getFailedLoginAttempts() { return failedLoginAttempts == null ? 0 : failedLoginAttempts; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public int getTokenVersion() { return tokenVersion == null ? 0 : tokenVersion; }
    public String getResetCodeHash() { return resetCodeHash; }
    public LocalDateTime getResetCodeExpiresAt() { return resetCodeExpiresAt; }
    public boolean isLocked() { return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now()); }
    public void recordFailedLogin() {
        if (lockedUntil != null && !lockedUntil.isAfter(LocalDateTime.now())) {
            failedLoginAttempts = 0; lockedUntil = null;
        }
        failedLoginAttempts = getFailedLoginAttempts() + 1;
        if (failedLoginAttempts >= 5) {
            failedLoginAttempts = 0; lockedUntil = LocalDateTime.now().plusMinutes(15);
        }
    }
    public void clearFailedLogins() { failedLoginAttempts = 0; lockedUntil = null; }
    public void changePassword(String encodedPassword) {
        password = encodedPassword; tokenVersion = getTokenVersion() + 1; clearFailedLogins();
        resetCodeHash = null; resetCodeExpiresAt = null;
    }
    public void issueResetCode(String hash, LocalDateTime expiresAt) {
        resetCodeHash = hash; resetCodeExpiresAt = expiresAt;
    }
}
