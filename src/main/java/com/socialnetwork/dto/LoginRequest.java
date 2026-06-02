package com.socialnetwork.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {
    @NotBlank
    private final String username;

    @NotBlank
    @Size(min = 1, max = 128)
    private final String password;

    @JsonCreator
    public LoginRequest(
        @JsonProperty("username") String username,
        @JsonProperty("password") String password
    ) {
        this.username = username;
        this.password = password;
    }
}
