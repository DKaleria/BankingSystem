package by.grgu.identityservice.database.entity.enumm;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER, ADMIN, MANAGER;

    @Override
    public String getAuthority() {
        return name();
    }
}
