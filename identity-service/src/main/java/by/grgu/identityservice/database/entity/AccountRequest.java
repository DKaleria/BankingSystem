package by.grgu.identityservice.database.entity;

import by.grgu.identityservice.database.entity.enumm.Role;
import lombok.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
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