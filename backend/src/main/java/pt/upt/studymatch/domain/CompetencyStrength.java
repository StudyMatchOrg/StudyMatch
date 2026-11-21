package pt.upt.studymatch.domain;

/**
 * How strong a group is in one competency, and which member carries it. Part of the explanation
 * of why students were grouped together.
 *
 * @param strength  (best + 0.5 × second-best) / 1.5, see {@link GroupScorer}
 * @param carriedBy name of the member with the highest score in this competency
 */
public record CompetencyStrength(String code, String name, double importance, double strength, String carriedBy) {
}
