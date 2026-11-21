package pt.upt.studymatch.domain;

import java.util.Map;

/**
 * A student as the grouping algorithm sees them: one effective score (0 to 1) per relevant
 * competency, built by {@link MatchingProfileBuilder}.
 *
 * @param scores      effective score per competency code
 * @param overall     importance-weighted average of the effective scores
 * @param lowEvidence true when no relevant competency has a grade yet (only teacher estimates or
 *                    nothing): a cold-start student
 */
public record MatchingStudent(Long studentId, String studentNumber, String name, Map<String, Double> scores,
        double overall, boolean lowEvidence) {

    public MatchingStudent {
        scores = Map.copyOf(scores);
    }

    public double score(String competencyCode) {
        return scores.getOrDefault(competencyCode, 0.0);
    }
}
