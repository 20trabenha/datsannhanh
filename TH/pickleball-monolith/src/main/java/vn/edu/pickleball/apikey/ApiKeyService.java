package vn.edu.pickleball.apikey;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.pickleball.apikey.ApiKeyDtos.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ApiKeyService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ApiKeyRepository keys;
    public ApiKeyService(ApiKeyRepository keys) { this.keys = keys; }
    @Transactional
    public ApiKeyResponse create(CreateRequest request) {
        if (request == null || request.ownerName() == null || request.ownerName().isBlank())
            throw new IllegalArgumentException("Tên đối tác không được để trống.");
        if (request.validDays() != null && request.validDays() < 1)
            throw new IllegalArgumentException("Số ngày hiệu lực phải lớn hơn 0.");
        byte[] random = new byte[32]; RANDOM.nextBytes(random);
        String raw = "pb_" + Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        LocalDateTime expires = request.validDays() == null ? null : LocalDateTime.now().plusDays(request.validDays());
        ApiKey key = keys.save(new ApiKey(hash(raw), request.ownerName().trim(), "courts:read", expires));
        return response(key, raw);
    }
    public List<ApiKeyResponse> list() { return keys.findAll().stream().map(k -> response(k, null)).toList(); }
    @Transactional
    public void revoke(Long id) {
        ApiKey key = keys.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy API Key."));
        key.revoke();
    }
    public boolean canReadCourts(String raw) {
        if (raw == null || raw.isBlank()) return false;
        return keys.findByKeyHash(hash(raw)).filter(k -> k.getStatus().equals("ACTIVE"))
                .filter(k -> k.getExpiresAt() == null || k.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(k -> k.getScopes().contains("courts:read")).isPresent();
    }
    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException("Không thể tạo API Key."); }
    }
    private ApiKeyResponse response(ApiKey key, String raw) {
        return new ApiKeyResponse(key.getId(), key.getOwnerName(), key.getScopes(), key.getStatus(),
                key.getCreatedAt(), key.getExpiresAt(), raw);
    }
}
