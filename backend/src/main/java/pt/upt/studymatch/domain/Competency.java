package pt.upt.studymatch.domain;

import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * Something a student can be good at, e.g. Programming or Databases. Competencies are data,
 * not hard-coded, so new ones can be added without changing the code.
 */
@Entity
public class Competency {

    private static final Pattern CODE = Pattern.compile("[A-Z0-9_]{2,20}");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    protected Competency() {
        // required by JPA
    }

    public Competency(String code, String name, String description) {
        if (code == null || !CODE.matcher(code).matches()) {
            throw new BusinessRuleException(
                    "Competency code must have 2 to 20 characters: capital letters, digits or '_', but was " + code);
        }
        this.code = code;
        change(name, description);
    }

    /** The code identifies the competency and does not change; the name and description can. */
    public void change(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("A competency needs a name");
        }
        this.name = name.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
