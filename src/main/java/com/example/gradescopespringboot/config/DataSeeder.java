package com.example.gradescopespringboot.config;

import com.example.gradescopespringboot.entity.Role;
import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.entity.UserRole;
import com.example.gradescopespringboot.service.RoleService;
import com.example.gradescopespringboot.service.UserRoleService;
import com.example.gradescopespringboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Development-only data seeder.
 *
 * <p>Creates a small set of sample roles and users on application startup so the
 * frontend auth pages can be tested immediately without manual registration.
 * The seeder is idempotent: existing users and roles are skipped on restart.</p>
 */
@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserService userService,
                      RoleService roleService,
                      UserRoleService userRoleService,
                      PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting data seeding...");
        seedRoles();
        seedUsers();
        log.info("Data seeding completed.");
    }

    private void seedRoles() {
        Map<String, String> roles = Map.of(
                "ADMIN", "System Administrator",
                "STUDENT", "Student",
                "TA", "Teaching Assistant",
                "INSTRUCTOR", "Instructor"
        );

        roles.forEach((code, name) -> {
            if (roleService.getByCode(code).isPresent()) {
                log.info("Role '{}' already exists, skipping.", code);
                return;
            }
            Role role = new Role();
            role.setRoleCode(code);
            role.setRoleName(name);
            role.setStatus(1);
            roleService.save(role);
            log.info("Created role '{}' with id={}", code, role.getId());
        });
    }

    private void seedUsers() {
        seedUser("alice", "password123", "Alice Chen", "alice@example.com", "S2026001", "STUDENT");
        seedUser("bob", "password123", "Bob Wang", "bob@example.com", "T2026001", "INSTRUCTOR");
        seedUser("charlie", "password123", "Charlie Liu", "charlie@example.com", "A2026001", "ADMIN");
    }

    private void seedUser(String username, String rawPassword, String realName,
                          String email, String userNo, String roleCode) {
        User existing = userService.getByUsername(username);
        if (existing != null) {
            log.info("User '{}' already exists, skipping user creation.", username);
            assignRoleIfMissing(existing.getId(), roleCode, username);
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

        assignRoleIfMissing(user.getId(), roleCode, username);
    }

    private void assignRoleIfMissing(Long userId, String roleCode, String username) {
        boolean hasRoles = !userRoleService.getByUserId(userId).isEmpty();
        if (hasRoles) {
            log.info("User '{}' already has roles, skipping role assignment.", username);
            return;
        }

        Role role = roleService.getByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleCode));
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRoleService.assignRole(userRole);
        log.info("Assigned role '{}' to user '{}'", roleCode, username);
    }
}
