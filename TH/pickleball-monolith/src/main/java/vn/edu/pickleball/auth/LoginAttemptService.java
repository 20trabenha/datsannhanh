package vn.edu.pickleball.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {
    private final UserRepository users;
    public LoginAttemptService(UserRepository users) { this.users = users; }
    @Transactional public void fail(Long userId) { users.findLockedById(userId).ifPresent(User::recordFailedLogin); }
    @Transactional public void clear(Long userId) { users.findLockedById(userId).ifPresent(User::clearFailedLogins); }
}
