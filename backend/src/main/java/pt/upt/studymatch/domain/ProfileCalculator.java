package pt.upt.studymatch.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Calculates a student's profile from all available evidence: the best grade of each course
 * unit ({@link AcademicEvidence}) and the teacher's initial assessments ({@link ColdStartPolicy}),
 * combined per competency by {@link EvidenceAggregator}.
 *
 * <p>Plain Java with no Spring or database dependency, so it is easy to test and to replace.
 */
public final class ProfileCalculator {

    private final AcademicEvidence academicEvidence = new AcademicEvidence();
    private final ColdStartPolicy coldStartPolicy = new ColdStartPolicy();
    private final EvidenceAggregator aggregator = new EvidenceAggregator();

    /**
     * @param competencies all competencies; each one appears in the profile, even when unknown
     * @param records      the student's academic records
     * @param links        how course units contribute to competencies
     * @param assessments  the student's initial assessments
     */
    public StudentProfile calculate(List<Competency> competencies, List<AcademicRecord> records,
            List<CourseUnitCompetency> links, List<CompetencyAssessment> assessments) {
        Map<String, List<Evidence>> academic = academicEvidence.byCompetency(records, links);
        List<StudentProfile.CompetencyScore> scores = competencies.stream()
                .sorted(Comparator.comparing(Competency::getName))
                .map(competency -> {
                    List<Evidence> evidence = new ArrayList<>(academic.getOrDefault(competency.getCode(), List.of()));
                    assessments.stream()
                            .filter(a -> a.getCompetency().getCode().equals(competency.getCode()))
                            .map(coldStartPolicy::toEvidence)
                            .forEach(evidence::add);
                    return new StudentProfile.CompetencyScore(competency, aggregator.aggregate(evidence));
                })
                .toList();
        return new StudentProfile(scores);
    }
}
