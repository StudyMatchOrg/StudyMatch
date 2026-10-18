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
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * States how much a course unit develops a competency. The weight is greater than 0 and at
 * most 1: 1 means the course unit is fully about the competency, 0.5 that it is half about it.
 * A course unit that does not develop a competency simply has no link to it.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"course_unit_id", "competency_id"}))
public class CourseUnitCompetency {

    public static final double MAX_WEIGHT = 1.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_unit_id", nullable = false)
    private CourseUnit courseUnit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @Column(nullable = false)
    private double weight;

    protected CourseUnitCompetency() {
        // required by JPA
    }

    public CourseUnitCompetency(CourseUnit courseUnit, Competency competency, double weight) {
        if (weight <= 0 || weight > MAX_WEIGHT) {
            throw new BusinessRuleException("Competency weight must be greater than 0 and at most 1, but was " + weight);
        }
        this.courseUnit = courseUnit;
        this.competency = competency;
        this.weight = weight;
    }

    public Long getId() {
        return id;
    }

    public CourseUnit getCourseUnit() {
        return courseUnit;
    }

    public Competency getCompetency() {
        return competency;
    }

    public double getWeight() {
        return weight;
    }
}
