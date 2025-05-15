package by.grgu.adminservice.dto;

import java.time.LocalDate;

public class AccountDTO {
    private String username;
    private LocalDate birthDate;
    private String firstname;
    private String lastname;
    private String password;
    private String email;
    private LocalDate registrationDate;
    private boolean active;
    private String role;

    public AccountDTO() {}

    public AccountDTO(String username, LocalDate birthDate, String firstname, String lastname, String password, String email, LocalDate registrationDate, boolean active, String role) {
        this.username = username;
        this.birthDate = birthDate;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.email = email;
        this.registrationDate = registrationDate;
        this.active = active;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}