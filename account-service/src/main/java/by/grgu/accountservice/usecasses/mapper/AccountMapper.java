package by.grgu.accountservice.usecasses.mapper;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import by.grgu.accountservice.database.enumm.Role;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface AccountMapper {

    @Mapping(target = "registrationDate", expression = "java(java.time.LocalDate.now())") // Устанавливаем текущую дату при регистрации
    @Mapping(target = "active", constant = "true") // Устанавливаем активность по умолчанию
    Account toAccount(AccountRequest request);

    default Role mapRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}