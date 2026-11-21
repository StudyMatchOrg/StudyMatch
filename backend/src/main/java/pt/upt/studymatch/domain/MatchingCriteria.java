package pt.upt.studymatch.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * The competencies that matter for a project, and how much each one matters. Different projects
 * can stress different competencies, e.g. a database project gives Databases a high importance.
 */
public record MatchingCriteria(List<Criterion> criteria) {

    public MatchingCriteria {
        if (criteria == null || criteria.isEmpty()) {
            throw new BusinessRuleException("At least one competency must be relevant for the project");
        }
        Set<String> codes = new HashSet<>();
        for (Criterion criterion : criteria) {
            if (!codes.add(criterion.code())) {
                throw new BusinessRuleException("Competency " + criterion.code() + " is listed twice");
            }
        }
        criteria = List.copyOf(criteria);
    }

    public double totalImportance() {
        return criteria.stream().mapToDouble(Criterion::importance).sum();
    }

    /** A relevant competency. The importance is greater than 0 and at most 1. */
    public record Criterion(String code, String name, double importance) {

        public Criterion {
            if (!(importance > 0 && importance <= 1)) {
                throw new BusinessRuleException("Importance of " + name
                        + " must be greater than 0 and at most 1, but was " + importance);
            }
        }
    }
}
