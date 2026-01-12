package com.gonga.tcc.microsservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenResponseDTO {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;

    public static TokenResponseDTO of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponseDTO(accessToken, refreshToken, "Bearer", expiresIn);
    }

    public static TokenResponseDTO ofAccessToken(String accessToken, long expiresIn) {
        return new TokenResponseDTO(accessToken, null, "Bearer", expiresIn);
    }
}
