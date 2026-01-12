package com.gonga.tcc.microsservice.dtos;

import java.time.LocalDateTime;

import com.gonga.tcc.microsservice.domain.UserRole;
import com.gonga.tcc.microsservice.domain.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserResponseDTO {

    private final String id;
    private final String name;
    private final String email;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

}
