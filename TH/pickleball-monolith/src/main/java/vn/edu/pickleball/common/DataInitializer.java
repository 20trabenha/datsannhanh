package vn.edu.pickleball.common;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.pickleball.auth.*;
import vn.edu.pickleball.court.*;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initialData(UserRepository users, CategoryRepository categories,
                                  CourtRepository courts, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByUsername("admin"))
                users.save(new User("admin", encoder.encode("admin123"), null, Role.ADMIN));
            if (categories.count() == 0) {
                Category indoor = categories.save(new Category("Trong nhà"));
                Category outdoor = categories.save(new Category("Ngoài trời"));
                if (courts.count() == 0) {
                    courts.save(new Court("Sân Pickleball 1", 120000, "Sân trong nhà", indoor));
                    courts.save(new Court("Sân Pickleball 2", 100000, "Sân ngoài trời", outdoor));
                }
            }
        };
    }
}
