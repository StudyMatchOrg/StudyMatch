package pt.upt.studymatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * A student in a generated group, with what the algorithm knew about them at that moment: their
 * overall score for the project, their strongest relevant competency, and whether their profile
 * had little evidence (cold start).
 */
@Entity
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private double overallScore;

    @Column(nullable = false)
    private boolean lowEvidence;

    @Column(length = 100)
    private String strongestCompetency;

    protected GroupMember() {
        // required by JPA
    }

    GroupMember(StudentGroup group, Student student, double overallScore, boolean lowEvidence,
            String strongestCompetency) {
        this.group = group;
        this.student = student;
        this.overallScore = overallScore;
        this.lowEvidence = lowEvidence;
        this.strongestCompetency = strongestCompetency;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public boolean isLowEvidence() {
        return lowEvidence;
    }

    public String getStrongestCompetency() {
        return strongestCompetency;
    }
}
