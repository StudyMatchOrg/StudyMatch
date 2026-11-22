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

/**
 * A competency that matters for a project, with its importance (greater than 0 and at most 1).
 * The grouping algorithm only looks at these competencies.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "competency_id"}))
public class ContextCompetency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "context_id", nullable = false)
    private GroupingContext context;

    @ManyToOne(optional = false)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @Column(nullable = false)
    private double importance;

    protected ContextCompetency() {
        // required by JPA
    }

    ContextCompetency(GroupingContext context, Competency competency, double importance) {
        // Validated by the same rule the algorithm uses.
        new MatchingCriteria.Criterion(competency.getCode(), competency.getName(), importance);
        this.context = context;
        this.competency = competency;
        this.importance = importance;
    }

    public Long getId() {
        return id;
    }

    public Competency getCompetency() {
        return competency;
    }

    public double getImportance() {
        return importance;
    }
}
