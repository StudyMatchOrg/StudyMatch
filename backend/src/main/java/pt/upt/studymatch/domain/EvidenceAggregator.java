package pt.upt.studymatch.domain.profile;

import java.util.Comparator;
import java.util.List;

/**
 * Combines all evidence about one competency with a single rule:
 *
 * <pre>
 *   score      = Σ value × weight / Σ weight
 *   confidence = W / (W + k)        with W = Σ weight and k = {@value #CONFIDENCE_CONSTANT}
 * </pre>
 *
 * <p>Academic results and cold-start evidence (teacher assessments) are treated the same way;
 * they only differ in weight. A teacher assessment is worth one standard course unit, so with no
 * grades it is the whole score, after one course unit it is half of it, after three a quarter.
 * The cold-start influence therefore fades automatically as academic evidence arrives. In
 * statistics this is known as shrinkage towards a prior.
 *
 * <p>With no evidence the score is unknown ({@code null}), never 0: a missing grade says nothing
 * about the student's ability.
 */
public final class EvidenceAggregator {

    /** Total weight at which the confidence reaches 0.5. */
    public static final double CONFIDENCE_CONSTANT = 2.0;

    public AggregatedScore aggregate(List<Evidence> evidence) {
        if (evidence.isEmpty()) {
            return new AggregatedScore(null, 0, ProfileStatus.UNKNOWN, 0, List.of());
        }
        double totalWeight = evidence.stream().mapToDouble(Evidence::weight).sum();
        double weightedSum = evidence.stream().mapToDouble(e -> e.value() * e.weight()).sum();
        List<AggregatedScore.Contribution> contributions = evidence.stream()
                .map(e -> new AggregatedScore.Contribution(e, e.weight() / totalWeight))
                .sorted(Comparator.comparingDouble(AggregatedScore.Contribution::share).reversed())
                .toList();
        return new AggregatedScore(weightedSum / totalWeight, confidence(totalWeight), status(evidence),
                totalWeight, contributions);
    }

    public static double confidence(double totalWeight) {
        return totalWeight / (totalWeight + CONFIDENCE_CONSTANT);
    }

    private static ProfileStatus status(List<Evidence> evidence) {
        boolean academic = evidence.stream().anyMatch(e -> e.type() == EvidenceType.ACADEMIC_RESULT);
        boolean initial = evidence.stream().anyMatch(e -> e.type() != EvidenceType.ACADEMIC_RESULT);
        if (academic && initial) {
            return ProfileStatus.BLENDED;
        }
        return academic ? ProfileStatus.ACADEMIC : ProfileStatus.INITIAL_ONLY;
    }
}
