package by.grgu.accountservice.database.entity;

import by.grgu.accountservice.database.enumm.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountRequest {
    private String username;

    private LocalDate birthDate;

    private String firstname;

    private String lastname;

    private String password;

    private String email;

    private LocalDate registrationDate;

    private boolean active;

    private Role role;
}