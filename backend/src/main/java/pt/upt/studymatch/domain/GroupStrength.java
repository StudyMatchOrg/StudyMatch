package pt.upt.studymatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * A saved {@link CompetencyStrength}: how strong a group was in one competency when it was
 * generated. Saved as a copy, so the explanation of past groups does not change when profiles do.
 */
@Embeddable
public class GroupStrength {

    @Column(name = "competency_code", nullable = false, length = 20)
    private String code;

    @Column(name = "competency_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private double importance;

    @Column(nullable = false)
    private double strength;

    @Column(length = 100)
    private String carriedBy;

    protected GroupStrength() {
        // required by JPA
    }

    GroupStrength(CompetencyStrength strength) {
        this.code = strength.code();
        this.name = strength.name();
        this.importance = strength.importance();
        this.strength = strength.strength();
        this.carriedBy = strength.carriedBy();
    }

    public CompetencyStrength toCompetencyStrength() {
        return new CompetencyStrength(code, name, importance, strength, carriedBy);
    }
}
