package com.gonga.tcc.microsservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.gonga.tcc.microsservice.domain.User;
import com.gonga.tcc.microsservice.dtos.UserRequestDTO;
import com.gonga.tcc.microsservice.dtos.UserResponseDTO;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponseDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserRequestDTO userRequestDTO);

}
