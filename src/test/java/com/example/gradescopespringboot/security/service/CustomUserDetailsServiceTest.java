package com.example.gradescopespringboot.security.service;

import com.example.gradescopespringboot.entity.Role;
import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.entity.UserRole;
import com.example.gradescopespringboot.security.model.LoginUser;
import com.example.gradescopespringboot.service.RoleService;
import com.example.gradescopespringboot.service.UserRoleService;
import com.example.gradescopespringboot.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_mapsRolesToGrantedAuthorities() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPasswordHash("encoded");
        user.setStatus(1);
        user.setIsDeleted(0);

        when(userService.getByUsername("alice")).thenReturn(user);
        when(userRoleService.getByUserId(1L)).thenReturn(List.of(createUserRole(1L, 2L)));
        when(roleService.getById(2L)).thenReturn(Optional.of(createRole(2L, "STUDENT")));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("alice");

        assertThat(userDetails).isInstanceOf(LoginUser.class);
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertThat(authorities).containsExactly("ROLE_STUDENT");
    }

    private UserRole createUserRole(Long id, Long roleId) {
        UserRole userRole = new UserRole();
        userRole.setId(id);
        userRole.setUserId(1L);
        userRole.setRoleId(roleId);
        return userRole;
    }

    private Role createRole(Long id, String roleCode) {
        Role role = new Role();
        role.setId(id);
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus(1);
        return role;
    }
}
