package by.grgu.identityservice.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {
    private String username;
    private LocalDate birthDate;
    private String email;
    private String firstname;
    private String lastname;
    private String password;
    private String role;
}
