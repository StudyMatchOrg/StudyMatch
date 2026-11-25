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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One generated group. Named StudentGroup because GROUP is a reserved word in SQL. Members are kept
 * strongest first, and the group's strength in each relevant competency is saved with it.
 */
@Entity
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_set_id", nullable = false)
    private GroupSet groupSet;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private double score;

    // Members are saved strongest first, so their ids keep that order.
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<GroupMember> members = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "student_group_strength", joinColumns = @JoinColumn(name = "group_id"))
    @OrderColumn(name = "position")
    private List<GroupStrength> strengths = new ArrayList<>();

    protected StudentGroup() {
        // required by JPA
    }

    StudentGroup(GroupSet groupSet, int number, double score) {
        this.groupSet = groupSet;
        this.number = number;
        this.score = score;
    }

    void addMember(Student student, double overallScore, boolean lowEvidence, String strongestCompetency) {
        members.add(new GroupMember(this, student, overallScore, lowEvidence, strongestCompetency));
    }

    void addStrength(CompetencyStrength strength) {
        strengths.add(new GroupStrength(strength));
    }

    public Long getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public double getScore() {
        return score;
    }

    public List<GroupMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<CompetencyStrength> getStrengths() {
        return strengths.stream().map(GroupStrength::toCompetencyStrength).toList();
    }
}
