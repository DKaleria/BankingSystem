package by.grgu.accountservice.database.entity;

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

    private String role;
}