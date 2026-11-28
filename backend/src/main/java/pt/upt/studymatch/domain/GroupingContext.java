package pt.upt.studymatch.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * A grouping context, shown to teachers as a "project": the situation in which groups are formed
 * (a course-unit project, an assignment, a practical class...). It defines which students take
 * part, the group size chosen by the teacher, and which competencies matter, with their
 * importance. A project belongs to the teacher who created it.
 */
@Entity
public class GroupingContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "course_unit_id")
    private CourseUnit courseUnit;

    @Column(length = 9)
    private String academicYear;

    @Column(nullable = false)
    private int groupSize;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "grouping_context_student",
            joinColumns = @JoinColumn(name = "context_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    private Set<Student> students = new HashSet<>();

    @OneToMany(mappedBy = "context", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContextCompetency> competencies = new ArrayList<>();

    protected GroupingContext() {
        // required by JPA
    }

    public GroupingContext(String name, String description, CourseUnit courseUnit, String academicYear,
            int groupSize, UserAccount owner, LocalDateTime createdAt) {
        this.owner = owner;
        this.createdAt = createdAt;
        change(name, description, courseUnit, academicYear, groupSize);
    }

    public void change(String name, String description, CourseUnit courseUnit, String academicYear, int groupSize) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("A project needs a name");
        }
        if (groupSize < GroupSizePlanner.MIN_GROUP_SIZE || groupSize > GroupSizePlanner.MAX_GROUP_SIZE) {
            throw new BusinessRuleException("Group size must be between " + GroupSizePlanner.MIN_GROUP_SIZE
                    + " and " + GroupSizePlanner.MAX_GROUP_SIZE + ", but was " + groupSize);
        }
        boolean hasYear = academicYear != null && !academicYear.isBlank();
        if (hasYear) {
            AcademicRecord.validateAcademicYear(academicYear);
        }
        this.name = name.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.courseUnit = courseUnit;
        this.academicYear = hasYear ? academicYear : null;
        this.groupSize = groupSize;
    }

    /** The eligible students. Every one of them is placed in a group when groups are generated. */
    public void replaceStudents(List<Student> eligible) {
        Set<Long> seen = new HashSet<>();
        for (Student student : eligible) {
            if (!seen.add(student.getId())) {
                throw new BusinessRuleException("Student " + student.getStudentNumber() + " is selected twice");
            }
        }
        students.clear();
        students.addAll(eligible);
    }

    /** Used when a student is deleted. Saved groups keep their own copy of who was in them. */
    public void removeStudent(Student student) {
        students.removeIf(s -> s.getId().equals(student.getId()));
    }

    /** The competencies that matter for this project and their importance. At least one is needed. */
    public void replaceCompetencies(Map<Competency, Double> importance) {
        if (importance.isEmpty()) {
            throw new BusinessRuleException("At least one competency must be relevant for the project");
        }
        competencies.clear();
        importance.forEach((competency, value) -> competencies.add(new ContextCompetency(this, competency, value)));
    }

    public boolean isVisibleTo(UserAccount account) {
        return account.isAdmin() || owner.getId().equals(account.getId());
    }

    /** What the grouping algorithm needs to know about the relevant competencies. */
    public MatchingCriteria criteria() {
        return new MatchingCriteria(getCompetencies().stream()
                .map(c -> new MatchingCriteria.Criterion(c.getCompetency().getCode(), c.getCompetency().getName(),
                        c.getImportance()))
                .toList());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CourseUnit getCourseUnit() {
        return courseUnit;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** The eligible students, by name. */
    public List<Student> getStudents() {
        return students.stream().sorted(Comparator.comparing(Student::getName)).toList();
    }

    /** The relevant competencies, most important first. */
    public List<ContextCompetency> getCompetencies() {
        List<ContextCompetency> sorted = new ArrayList<>(competencies);
        sorted.sort(Comparator.comparingDouble(ContextCompetency::getImportance).reversed()
                .thenComparing(c -> c.getCompetency().getName()));
        return Collections.unmodifiableList(sorted);
    }

    /** Importance per competency, in the order of {@link #getCompetencies()}. */
    public Map<Competency, Double> importanceByCompetency() {
        Map<Competency, Double> importance = new LinkedHashMap<>();
        getCompetencies().forEach(c -> importance.put(c.getCompetency(), c.getImportance()));
        return importance;
    }
}
