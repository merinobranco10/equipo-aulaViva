package cl.aulaviva.identity;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Usuario autenticado: lleva su tenant para filtrar todas las consultas. */
public record AulaVivaUser(String id, String tenantId, String username, String passwordHash,
        String fullName, Role role) implements UserDetails {

    public static AulaVivaUser from(AppUser user) {
        return new AulaVivaUser(user.getId(), user.getTenantId(), user.getUsername(),
            user.getPasswordHash(), user.getFullName(), user.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "AulaVivaUser[" + username + ", " + role + ", tenant=" + tenantId + "]";
    }
}
