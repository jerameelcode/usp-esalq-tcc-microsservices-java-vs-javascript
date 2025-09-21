package com.gonga.tcc.microsservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gonga.tcc.microsservice.dtos.UserRequestDTO;
import com.gonga.tcc.microsservice.dtos.UserResponseDTO;
import com.gonga.tcc.microsservice.mapper.UserMapper;
import com.gonga.tcc.microsservice.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private static final UserMapper userMapper = UserMapper.INSTANCE;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        var userEntity = userMapper.toEntity(userRequestDTO);
        var savedUser = userRepository.save(userEntity);
        return userMapper.toDTO(savedUser);
    }

}
