package com.music.mapper;

import com.music.dto.RoleDTO;
import com.music.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);
    
    RoleDTO toDto(Role role);
    Role toEntity(RoleDTO roleDTO);
} 