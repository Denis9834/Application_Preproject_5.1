package ru.max.springboot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.max.springboot.model.Role;
import ru.max.springboot.repository.RoleRepository;
import ru.max.springboot.service.RoleService;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    //Список всех ролей
    @Override
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    //Поиск роли по id
    @Override
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    //Поиск роли по названию
    @Override
    public Optional<Role> findByRole(String roleName) {
        return roleRepository.findByRole(roleName);
    }
}
