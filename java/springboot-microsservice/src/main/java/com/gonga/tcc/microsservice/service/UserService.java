package com.gonga.tcc.microsservice.service;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gonga.tcc.microsservice.domain.User;
import com.gonga.tcc.microsservice.dtos.UserRequestDTO;
import com.gonga.tcc.microsservice.dtos.UserResponseDTO;
import com.gonga.tcc.microsservice.exceptions.ResourceNotFoundException;
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

    public UserResponseDTO updateUser(UUID id, UserRequestDTO userRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(userRequestDTO.getPassword());

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    public UserResponseDTO partialUpdateUser(UUID id, Map<String, Object> updates) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        updates.forEach((key, value) -> {
            try {
                PropertyDescriptor pd = new PropertyDescriptor(key, User.class);
                Method setter = pd.getWriteMethod();
                if (setter != null) {
                    setter.invoke(user, value);
                } else {
                    throw new IllegalArgumentException("Field does not have a setter: " + key);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid field: " + key, e);
            }
        });

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

}
