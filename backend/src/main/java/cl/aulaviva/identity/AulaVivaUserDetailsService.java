package cl.aulaviva.identity;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AulaVivaUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public AulaVivaUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return users.findByUsername(username)
            .map(AulaVivaUser::from)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
