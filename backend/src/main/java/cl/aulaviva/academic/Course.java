package cl.aulaviva.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    private String id;

    private String tenantId;

    private String name;

    private String teacherId;

    protected Course() {
    }

    public Course(String id, String tenantId, String name, String teacherId) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.teacherId = teacherId;
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getTeacherId() {
        return teacherId;
    }
}
