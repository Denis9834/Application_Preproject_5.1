package ru.max.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.max.springboot.model.Role;
import ru.max.springboot.repository.RoleRepository;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    //Список всех ролей
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    //Поиск роли по id
    public Role findRoleById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    //Поиск роли по названию
    public Role findByRole(String roleName) {
        return roleRepository.findByRole(roleName);
    }
}
