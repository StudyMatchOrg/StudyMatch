package pt.upt.studymatch.domain.profile;

import java.util.List;

/**
 * The result of combining all evidence about one competency.
 *
 * @param score       weighted average of the evidence values, from 0 to 1; {@code null} when unknown
 * @param confidence  how much the score can be trusted, from 0 (no evidence) towards 1
 * @param status      which kinds of evidence the score is based on
 * @param totalWeight sum of the evidence weights
 * @param evidence    every piece of evidence with its share of the score, largest share first
 */
public record AggregatedScore(Double score, double confidence, ProfileStatus status, double totalWeight,
        List<Contribution> evidence) {

    /**
     * A piece of evidence and the fraction of the score it accounts for (its weight divided by
     * the total weight). The fractions of a score add up to 1.
     */
    public record Contribution(Evidence evidence, double share) {
    }

    public boolean isKnown() {
        return score != null;
    }
}
