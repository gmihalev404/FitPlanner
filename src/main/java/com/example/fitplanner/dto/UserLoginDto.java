package com.example.fitplanner.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Data
public class UserLoginDto implements Serializable {
    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    @Size(min = 4)
    private String password;
}
