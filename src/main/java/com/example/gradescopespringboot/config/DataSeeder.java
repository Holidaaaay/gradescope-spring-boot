package com.example.gradescopespringboot.config;

import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Development-only data seeder.
 *
 * <p>Creates a small set of sample users on application startup so the
 * frontend auth pages can be tested immediately without manual registration.
 * The seeder is idempotent: existing users are skipped on restart.</p>
 */
@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting data seeding...");
        seedUsers();
        log.info("Data seeding completed.");
    }

    private void seedUsers() {
        seedUser("alice", "password123", "Alice Chen", "alice@example.com", "S2026001");
        seedUser("bob", "password123", "Bob Wang", "bob@example.com", "T2026001");
        seedUser("charlie", "password123", "Charlie Liu", "charlie@example.com", "A2026001");
    }

    private void seedUser(String username, String rawPassword, String realName,
                          String email, String userNo) {
        if (userService.getByUsername(username) != null) {
            log.info("User '{}' already exists, skipping.", username);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRealName(realName);
        user.setEmail(email);
        user.setUserNo(userNo);
        user.setStatus(1);
        user.setIsDeleted(0);

        userService.save(user);
        log.info("Created user '{}' with id={}", username, user.getId());
    }
}
