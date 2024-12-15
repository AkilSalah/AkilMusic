package com.music.service.implementation;

import com.music.dto.RoleDTO;
import com.music.mapper.RoleMapper;
import com.music.model.Role;
import com.music.repository.RoleRepo;
import com.music.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepo roleRepo;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleDTO> getRoles() {
        List<Role> roles = roleRepo.findAll();
        return roleMapper.toDto(roles);
    }

    @Override
    public RoleDTO addRole(Role role) {
        roleRepo.findByName(role.getName()).orElseThrow(
                ()-> new IllegalArgumentException("This role already exist")
        );
        Role roleSaved = roleRepo.save(role);
        return roleMapper.toDto(roleSaved);
    }
}
