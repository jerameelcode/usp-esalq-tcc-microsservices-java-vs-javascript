package com.gonga.tcc.microsservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class  UserResponseDTO {

    private final String id;
    private final String name;
    private final String email;

}
