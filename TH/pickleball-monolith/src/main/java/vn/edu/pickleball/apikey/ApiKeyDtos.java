package vn.edu.pickleball.apikey;

import java.time.LocalDateTime;

public final class ApiKeyDtos {
    private ApiKeyDtos() {}
    public record CreateRequest(String ownerName, Integer validDays) {}
    public record ApiKeyResponse(Long id, String ownerName, String scopes, String status,
                                 LocalDateTime createdAt, LocalDateTime expiresAt, String keyValue) {}
}
