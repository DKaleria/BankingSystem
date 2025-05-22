package by.grgu.identityservice.usecaseses.mapper;

import by.grgu.identityservice.database.entity.RegistrationRequest;
import by.grgu.identityservice.database.entity.User;
import by.grgu.identityservice.database.entity.enumm.Role;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface AuthUserMapper {

    @Mapping(target = "role", expression = "java(mapRole(request.getRole()))")
    @Mapping(target = "password", expression = "java(encodePassword(request.getPassword(), passwordEncoder))")
    User toUser(RegistrationRequest request, PasswordEncoder passwordEncoder);

    default Role mapRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    default String encodePassword(String password, PasswordEncoder passwordEncoder) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        String encodedPassword = passwordEncoder.encode(password);
        return encodedPassword;
    }
}