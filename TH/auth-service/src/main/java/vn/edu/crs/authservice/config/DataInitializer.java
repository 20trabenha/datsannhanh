package vn.edu.crs.authservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.crs.authservice.entity.Role;
import vn.edu.crs.authservice.entity.User;
import vn.edu.crs.authservice.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("student1")) {
            User student = User.builder()
                    .username("student1")
                    .password(passwordEncoder.encode("student123"))
                    .role(Role.STUDENT)
                    .build();
            userRepository.save(student);
        }
    }
}
