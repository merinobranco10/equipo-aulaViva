package cl.aulaviva.tenancy;

import cl.aulaviva.identity.AulaVivaUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Entrega el usuario autenticado y su tenant. El tenant SIEMPRE sale de aqui (del usuario
 * autenticado), nunca de un parametro del request (ADR 0007).
 */
@Component
public class TenantContext {

    public AulaVivaUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AulaVivaUser user) {
            return user;
        }
        throw new IllegalStateException("No hay un usuario autenticado en el contexto");
    }

    public String tenantId() {
        return currentUser().tenantId();
    }
}
