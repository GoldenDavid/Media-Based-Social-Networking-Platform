package com.socialnetwork.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterRequest {
    @NotBlank
    @Size(max = 100)
    private final String name;

    @NotBlank
    @Size(min = 3, max = 50)
    private final String username;

    @NotBlank
    @Size(min = 6, max = 128)
    private final String password;

    @JsonCreator
    public RegisterRequest(
        @JsonProperty("name") String name,
        @JsonProperty("username") String username,
        @JsonProperty("password") String password
    ) {
        this.name = name;
        this.username = username;
        this.password = password;
    }
}
