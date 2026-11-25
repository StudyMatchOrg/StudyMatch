package pt.upt.studymatch.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * One generation of groups for a project: when and by whom it was generated, with which strategy,
 * seed and group size, its groups and how good they are. Every generation is kept, so earlier
 * groups can always be consulted (brief section 3.1), even after the project or the profiles change.
 */
@Entity
public class GroupSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "context_id", nullable = false)
    private GroupingContext context;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "generated_by_id", nullable = false)
    private UserAccount generatedBy;

    @Column(nullable = false, length = 20)
    private String strategy;

    @Column(nullable = false)
    private long seed;

    @Column(nullable = false)
    private int groupSize;

    @Column(nullable = false)
    private double weakestScore;

    @Column(nullable = false)
    private double averageScore;

    @Column(nullable = false)
    private double spread;

    @Column(nullable = false)
    private double randomWeakestScore;

    @ElementCollection
    @CollectionTable(name = "group_set_warning", joinColumns = @JoinColumn(name = "group_set_id"))
    @OrderColumn(name = "position")
    @Column(name = "warning", length = 300)
    private List<String> warnings = new ArrayList<>();

    @OneToMany(mappedBy = "groupSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number")
    private List<StudentGroup> groups = new ArrayList<>();

    protected GroupSet() {
        // required by JPA
    }

    private GroupSet(GroupingContext context, UserAccount generatedBy, String strategy, long seed,
            LocalDateTime generatedAt) {
        this.context = context;
        this.generatedBy = generatedBy;
        this.strategy = strategy;
        this.seed = seed;
        this.groupSize = context.getGroupSize();
        this.generatedAt = generatedAt;
    }

    /**
     * Saves the result of {@link GroupFormation}. Every member must be one of the project's students.
     */
    public static GroupSet of(GroupingContext context, UserAccount generatedBy, String strategy, long seed,
            GroupFormation.FormationResult result, LocalDateTime generatedAt) {
        GroupSet set = new GroupSet(context, generatedBy, strategy, seed, generatedAt);
        set.weakestScore = result.weakestScore();
        set.averageScore = result.averageScore();
        set.spread = result.spread();
        set.randomWeakestScore = result.randomWeakestScore();
        set.warnings.addAll(result.warnings());
        Map<Long, Student> students = context.getStudents().stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        Map<String, String> competencyNames = context.criteria().criteria().stream()
                .collect(Collectors.toMap(MatchingCriteria.Criterion::code, MatchingCriteria.Criterion::name));
        for (GroupFormation.FormedGroup formed : result.groups()) {
            StudentGroup group = new StudentGroup(set, formed.number(), formed.score());
            for (MatchingStudent member : formed.members()) {
                Student student = students.get(member.studentId());
                if (student == null) {
                    throw new IllegalArgumentException("Student " + member.studentNumber() + " is not in the project");
                }
                group.addMember(student, member.overall(), member.lowEvidence(),
                        strongest(member, competencyNames));
            }
            formed.strengths().forEach(group::addStrength);
            set.groups.add(group);
        }
        return set;
    }

    private static String strongest(MatchingStudent member, Map<String, String> competencyNames) {
        return member.scores().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> competencyNames.get(entry.getKey()))
                .orElse(null);
    }

    public int studentCount() {
        return groups.stream().mapToInt(g -> g.getMembers().size()).sum();
    }

    public Long getId() {
        return id;
    }

    public GroupingContext getContext() {
        return context;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public UserAccount getGeneratedBy() {
        return generatedBy;
    }

    public String getStrategy() {
        return strategy;
    }

    public long getSeed() {
        return seed;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public double getWeakestScore() {
        return weakestScore;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public double getSpread() {
        return spread;
    }

    public double getRandomWeakestScore() {
        return randomWeakestScore;
    }

    /** A copy, so it can still be read after the transaction that loaded the generation has ended. */
    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }

    public List<StudentGroup> getGroups() {
        return Collections.unmodifiableList(groups);
    }
}
