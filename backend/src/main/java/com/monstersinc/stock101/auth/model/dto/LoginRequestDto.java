package com.monstersinc.stock101.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class LoginRequestDto {

    @NotBlank
    @Schema(title = "아이디", example = "admin@naver.com")
    private final String email;

    @NotBlank
    @Schema(title = "아이디", example = "qwer1234")
    private final String password;
}
