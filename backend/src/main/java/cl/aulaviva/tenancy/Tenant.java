package cl.aulaviva.tenancy;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Un colegio = un tenant aislado logicamente. */
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    private String id;

    private String name;

    protected Tenant() {
    }

    public Tenant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
