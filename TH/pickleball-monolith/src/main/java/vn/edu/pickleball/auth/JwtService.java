package vn.edu.pickleball.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private final Key key;
    public JwtService(@Value("${app.jwt-secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String create(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder().setSubject(user.getUsername())
                .claim("userId", user.getId()).claim("role", user.getRole().name())
                .claim("tokenVersion", user.getTokenVersion())
                .setIssuedAt(new Date(now)).setExpiration(new Date(now + 86_400_000L))
                .signWith(key).compact();
    }
    public Claims verify(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
