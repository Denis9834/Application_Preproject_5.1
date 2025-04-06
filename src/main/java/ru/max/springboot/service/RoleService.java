package ru.max.springboot.service;

import ru.max.springboot.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<Role> findAllRoles();

    Optional<Role> getRoleById(Long id);

    Optional<Role> findByRole(String roleName);
}
