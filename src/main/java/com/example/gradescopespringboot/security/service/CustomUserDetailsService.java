package com.example.gradescopespringboot.security.service;

import com.example.gradescopespringboot.entity.Role;
import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.entity.UserRole;
import com.example.gradescopespringboot.security.model.LoginUser;
import com.example.gradescopespringboot.service.RoleService;
import com.example.gradescopespringboot.service.UserRoleService;
import com.example.gradescopespringboot.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;

    public CustomUserDetailsService(UserService userService,
                                    UserRoleService userRoleService,
                                    RoleService roleService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        List<String> roleCodes = loadRoleCodesByUserId(user.getId());
        return LoginUser.withRoleCodes(user, roleCodes);
    }

    private List<String> loadRoleCodesByUserId(Long userId) {
        List<Long> roleIds = userRoleService.getByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .toList();

        return roleIds.stream()
                .map(roleService::getById)
                .flatMap(java.util.Optional::stream)
                .map(Role::getRoleCode)
                .toList();
    }
}
