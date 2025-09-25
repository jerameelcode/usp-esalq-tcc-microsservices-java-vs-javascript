package com.gonga.tcc.microsservice.service;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Value("${user.not.found}")
    private String userNotFound;

    private static final String LOG_MSG_FORMAT = "{}: {}";

    private final UserRepository userRepository;
    private static final UserMapper userMapper = UserMapper.INSTANCE;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserResponseDTO> findAll(Pageable pageable) {
        logger.info("Fetching all users with pagination: {}", pageable);
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    public UserResponseDTO findById(UUID id) {
        logger.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            logger.error(LOG_MSG_FORMAT, userNotFound, id);
            return new ResourceNotFoundException(userNotFound+": " + id);
        });
        logger.info("User found with ID: {}", id);
        return userMapper.toDTO(user);
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        logger.info("Creating user with email: {}", userRequestDTO.getEmail());
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            logger.error("Email already exists: {}", userRequestDTO.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        var userEntity = userMapper.toEntity(userRequestDTO);
        var savedUser = userRepository.save(userEntity);
        logger.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toDTO(savedUser);
    }

    public UserResponseDTO updateUser(UUID id, UserRequestDTO userRequestDTO) {
        logger.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error(LOG_MSG_FORMAT, userNotFound, id);
                    return new ResourceNotFoundException(userNotFound+": " + id);
                });

        User userToCreate = userMapper.toEntity(userRequestDTO);
        userToCreate.setId(user.getId());

        User updatedUser = userRepository.save(userToCreate);
        logger.info("User updated successfully with ID: {}", updatedUser.getId());
        return userMapper.toDTO(updatedUser);
    }

    public UserResponseDTO partialUpdateUser(UUID id, Map<String, Object> updates) {
        logger.info("Partially updating user with ID: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            logger.error(LOG_MSG_FORMAT, userNotFound, id);
            return new ResourceNotFoundException(userNotFound+": " + id);
        });

        updates.forEach((key, value) -> {
            try {
                logger.info("Updating field '{}' with value '{}'", key, value);
                PropertyDescriptor pd = new PropertyDescriptor(key, User.class);
                Method setter = pd.getWriteMethod();
                if (setter != null) {
                    setter.invoke(user, value);
                } else {
                    throw new IllegalArgumentException("Field does not have a setter: " + key);
                }
            } catch (IllegalAccessException | IllegalArgumentException e) {
                logger.error("Error while updating field '{}' for user with ID '{}': {}", key, id, e.getMessage(), e);
                throw new IllegalArgumentException("Error while updating field: " + key + " for user with ID: " + id, e);
            } catch (Exception e) {
                logger.error("Unexpected error while updating field '{}': {}", key, e.getMessage(), e);
                throw new IllegalArgumentException("Unexpected error while updating field: " + key, e);
            }
        });

        User updatedUser = userRepository.save(user);
        logger.info("User partially updated successfully with ID: {}", updatedUser.getId());
        return userMapper.toDTO(updatedUser);
    }

}
