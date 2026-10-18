package pt.upt.studymatch.domain;

import pt.upt.studymatch.domain.CompetencyAssessment;

/**
 * Cold-start strategy: how information other than grades enters the profile.
 *
 * <p>A teacher assessment becomes a piece of evidence worth
 * {@value #TEACHER_ASSESSMENT_WEIGHT}, the same as one standard 6-ECTS course unit fully about
 * the competency. {@link EvidenceAggregator} then combines it with the grades, so its influence
 * shrinks as academic evidence accumulates. Other sources (for example self-assessment or
 * secondary-school grades) can be added later as new evidence types with their own weight.
 */
public final class ColdStartPolicy {

    public static final double TEACHER_ASSESSMENT_WEIGHT = 1.0;

    public Evidence toEvidence(CompetencyAssessment assessment) {
        return new Evidence(EvidenceType.TEACHER_ASSESSMENT, "Teacher assessment (" + assessment.getAssessedOn() + ")",
                assessment.getValue(), TEACHER_ASSESSMENT_WEIGHT);
    }
}
