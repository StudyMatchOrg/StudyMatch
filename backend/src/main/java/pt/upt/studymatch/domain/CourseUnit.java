package pt.upt.studymatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import pt.upt.studymatch.exception.BusinessRuleException;

// Course unit linked to degrees, using ECTS credits to weight student grades
@Entity
public class CourseUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private double ects;

    @Column(nullable = false)
    private int curricularYear;

    @Column(nullable = false)
    private int semester;

    @ManyToMany
    @JoinTable(
            name = "course_unit_degree",
            joinColumns = @JoinColumn(name = "course_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "degree_id"))
    private Set<Degree> degrees = new HashSet<>();

    protected CourseUnit() {}

    public CourseUnit(String code, String name, double ects, int curricularYear, int semester,
            Set<Degree> degrees) {
        if (ects <= 0) {
            throw new BusinessRuleException("ECTS must be greater than 0");
        }
        if (curricularYear < 1) {
            throw new BusinessRuleException("Curricular year must be at least 1");
        }
        if (semester != 1 && semester != 2) {
            throw new BusinessRuleException("Semester must be 1 or 2");
        }
        if (degrees == null || degrees.isEmpty()) {
            throw new BusinessRuleException("A course unit must belong to at least one degree");
        }
        this.code = code;
        this.name = name;
        this.ects = ects;
        this.curricularYear = curricularYear;
        this.semester = semester;
        this.degrees = new HashSet<>(degrees);
    }

    public boolean belongsTo(Degree degree) {
        return degrees.stream().anyMatch(d -> d.getCode().equals(degree.getCode()));
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

    public double getEcts() {
        return ects;
    }

    public int getCurricularYear() {
        return curricularYear;
    }

    public int getSemester() {
        return semester;
    }

    public Set<Degree> getDegrees() {
        return Collections.unmodifiableSet(degrees);
    }
}
