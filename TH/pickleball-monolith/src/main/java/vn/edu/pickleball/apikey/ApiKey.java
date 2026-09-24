package vn.edu.pickleball.apikey;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
public class ApiKey {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String keyHash;
    @Column(nullable = false)
    private String ownerName;
    @Column(nullable = false)
    private String scopes;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    protected ApiKey() {}
    public ApiKey(String keyHash, String ownerName, String scopes, LocalDateTime expiresAt) {
        this.keyHash = keyHash; this.ownerName = ownerName; this.scopes = scopes;
        this.expiresAt = expiresAt; this.status = "ACTIVE"; this.createdAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getKeyHash() { return keyHash; }
    public String getOwnerName() { return ownerName; }
    public String getScopes() { return scopes; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void revoke() { status = "REVOKED"; }
}
