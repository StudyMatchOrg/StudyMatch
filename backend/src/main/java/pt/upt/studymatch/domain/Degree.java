package pt.upt.studymatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import pt.upt.studymatch.exception.BusinessRuleException;

// A degree program such as Informatics Engineering
@Entity
public class Degree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    protected Degree() {}

    public Degree(String code, String name) {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new BusinessRuleException("A degree needs a code and a name");
        }
        this.code = code.trim();
        this.name = name.trim();
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
}
