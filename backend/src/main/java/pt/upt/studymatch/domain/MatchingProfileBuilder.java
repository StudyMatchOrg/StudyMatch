package pt.upt.studymatch.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Turns student profiles into the inputs of the grouping algorithm. This is where the cold-start
 * strategy meets group formation: a score is only trusted as much as its confidence, and the rest
 * is filled in with the class average:
 *
 * <pre>
 *   effective = confidence × score + (1 − confidence) × class average
 * </pre>
 *
 * <p>So a first-year student with only a teacher estimate (confidence 0.33) is placed mostly as
 * an average student, and an unknown competency counts exactly as the class average. Nobody is
 * treated as bad at a competency just because there is no information about it.
 *
 * <p>A student with no grades in any of the project's competencies (only teacher estimates, or
 * nothing) is marked as "low evidence", so the teacher can see who was placed by estimate.
 */
public final class MatchingProfileBuilder {

    /** Used for a competency that no student in the group of candidates has any evidence for. */
    public static final double NEUTRAL_SCORE = 0.5;

    public List<MatchingStudent> build(List<ProfiledStudent> students, MatchingCriteria criteria) {
        Map<String, Double> classAverage = new HashMap<>();
        for (MatchingCriteria.Criterion criterion : criteria.criteria()) {
            OptionalDouble average = students.stream()
                    .map(s -> s.profile().scoreOf(criterion.code()).orElse(null))
                    .filter(score -> score != null && score.isKnown())
                    .mapToDouble(AggregatedScore::score)
                    .average();
            classAverage.put(criterion.code(), average.orElse(NEUTRAL_SCORE));
        }
        return students.stream().map(s -> toMatchingStudent(s, criteria, classAverage)).toList();
    }

    private static MatchingStudent toMatchingStudent(ProfiledStudent profiled, MatchingCriteria criteria,
            Map<String, Double> classAverage) {
        Map<String, Double> scores = new HashMap<>();
        double weightedSum = 0;
        boolean hasGrades = false;
        for (MatchingCriteria.Criterion criterion : criteria.criteria()) {
            AggregatedScore score = profiled.profile().scoreOf(criterion.code()).orElse(null);
            double average = classAverage.get(criterion.code());
            double effective = average;
            if (score != null && score.isKnown()) {
                effective = score.confidence() * score.score() + (1 - score.confidence()) * average;
            }
            scores.put(criterion.code(), effective);
            weightedSum += criterion.importance() * effective;
            hasGrades |= score != null && score.status().hasAcademicEvidence();
        }
        Student student = profiled.student();
        return new MatchingStudent(student.getId(), student.getStudentNumber(), student.getName(), scores,
                weightedSum / criteria.totalImportance(), !hasGrades);
    }
}
