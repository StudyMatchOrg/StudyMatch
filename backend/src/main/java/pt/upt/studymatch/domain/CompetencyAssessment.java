package pt.upt.studymatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * A teacher's initial estimate of a student's level in a competency, from 0 to 1. It gives
 * students with little or no academic history a useful profile (cold start). A student has
 * at most one current assessment per competency.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "competency_id"}))
public class CompetencyAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @Column(name = "assessment_value", nullable = false)
    private double value;

    @Column(nullable = false)
    private LocalDate assessedOn;

    protected CompetencyAssessment() {
        // required by JPA
    }

    public CompetencyAssessment(Student student, Competency competency, double value, LocalDate assessedOn) {
        this.student = student;
        this.competency = competency;
        revise(value, assessedOn);
    }

    public void revise(double value, LocalDate assessedOn) {
        if (Double.isNaN(value) || value < 0 || value > 1) {
            throw new BusinessRuleException("Assessment value must be between 0 and 1, but was " + value);
        }
        this.value = value;
        this.assessedOn = assessedOn;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public Competency getCompetency() {
        return competency;
    }

    public double getValue() {
        return value;
    }

    public LocalDate getAssessedOn() {
        return assessedOn;
    }
}
