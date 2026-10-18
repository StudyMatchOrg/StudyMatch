package pt.upt.studymatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * One attempt of a student at a course unit in a given academic year and semester. A student
 * can have several records for the same course unit (failed and repeated, or grade
 * improvement), but only one per academic year and semester.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        columnNames = {"student_id", "course_unit_id", "academic_year", "semester"}))
public class AcademicRecord {

    public static final int MIN_GRADE = 0;
    public static final int PASSING_GRADE = 10;
    public static final int MAX_GRADE = 20;

    private static final Pattern ACADEMIC_YEAR = Pattern.compile("(\\d{4})/(\\d{4})");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_unit_id", nullable = false)
    private CourseUnit courseUnit;

    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;

    @Column(nullable = false)
    private int semester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RecordStatus status;

    private Integer grade;

    protected AcademicRecord() {
        // required by JPA
    }

    public AcademicRecord(Student student, CourseUnit courseUnit, String academicYear, int semester,
            RecordStatus status, Integer grade) {
        if (!courseUnit.belongsTo(student.getDegree())) {
            throw new BusinessRuleException("Course unit " + courseUnit.getCode()
                    + " is not part of the degree " + student.getDegree().getCode());
        }
        this.student = student;
        this.courseUnit = courseUnit;
        change(academicYear, semester, status, grade);
    }

    /**
     * Changes the attempt's period and result. The course unit and the student cannot change:
     * a different course unit is a different record.
     */
    public void change(String academicYear, int semester, RecordStatus status, Integer grade) {
        validateAcademicYear(academicYear);
        if (semester != 1 && semester != 2) {
            throw new BusinessRuleException("Semester must be 1 or 2");
        }
        validateResult(status, grade);
        this.academicYear = academicYear;
        this.semester = semester;
        this.status = status;
        this.grade = grade;
    }

    private static void validateAcademicYear(String academicYear) {
        Matcher matcher = academicYear == null ? null : ACADEMIC_YEAR.matcher(academicYear);
        if (matcher == null || !matcher.matches()
                || Integer.parseInt(matcher.group(2)) != Integer.parseInt(matcher.group(1)) + 1) {
            throw new BusinessRuleException("Academic year must look like 2025/2026, but was " + academicYear);
        }
    }

    private static void validateResult(RecordStatus status, Integer grade) {
        if (status == null) {
            throw new BusinessRuleException("Status is required");
        }
        if (status == RecordStatus.ENROLLED) {
            if (grade != null) {
                throw new BusinessRuleException("An ENROLLED record cannot have a grade yet");
            }
            return;
        }
        if (grade == null) {
            throw new BusinessRuleException("A " + status + " record needs a grade");
        }
        if (grade < MIN_GRADE || grade > MAX_GRADE) {
            throw new BusinessRuleException("Grade must be between 0 and 20, but was " + grade);
        }
        if (status == RecordStatus.PASSED && grade < PASSING_GRADE) {
            throw new BusinessRuleException("A PASSED record needs a grade of at least 10, but was " + grade);
        }
        if (status == RecordStatus.FAILED && grade >= PASSING_GRADE) {
            throw new BusinessRuleException("A FAILED record needs a grade below 10, but was " + grade);
        }
    }

    /** True when the record has a final grade, i.e. it is evidence for the student profile. */
    public boolean hasGrade() {
        return grade != null;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public CourseUnit getCourseUnit() {
        return courseUnit;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public int getSemester() {
        return semester;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public Integer getGrade() {
        return grade;
    }
}
