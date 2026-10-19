package pt.upt.studymatch.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Turns a student's academic records into evidence about competencies.
 *
 * <ul>
 *   <li>Only records with a grade count; ENROLLED records are not evidence yet.</li>
 *   <li>Only the best grade of each course unit counts: a grade improvement or a passed retake
 *       replaces earlier attempts. A course unit that was only failed still counts, with its
 *       grade, because it is real evidence of the student's level.</li>
 *   <li>The value is {@code grade / 20}.</li>
 *   <li>The weight is {@code competency weight × ECTS / 6}: a 6-ECTS course unit fully about a
 *       competency is worth 1.</li>
 * </ul>
 */
public final class AcademicEvidence {

    /** ECTS of a standard course unit, which is worth a weight of 1. */
    public static final double STANDARD_ECTS = 6.0;

    /** The best graded attempt of each course unit, keyed by course-unit code. */
    public Map<String, AcademicRecord> bestAttempts(List<AcademicRecord> records) {
        Comparator<AcademicRecord> better = Comparator.comparing(AcademicRecord::getGrade)
                .thenComparing(AcademicRecord::getAcademicYear)
                .thenComparing(AcademicRecord::getSemester);
        return records.stream()
                .filter(AcademicRecord::hasGrade)
                .collect(Collectors.toMap(r -> r.getCourseUnit().getCode(), Function.identity(),
                        (a, b) -> better.compare(a, b) >= 0 ? a : b, LinkedHashMap::new));
    }

    /** Evidence for each competency, keyed by competency code. */
    public Map<String, List<Evidence>> byCompetency(List<AcademicRecord> records, List<CourseUnitCompetency> links) {
        Map<String, AcademicRecord> best = bestAttempts(records);
        Map<String, List<Evidence>> evidence = new LinkedHashMap<>();
        for (CourseUnitCompetency link : links) {
            AcademicRecord record = best.get(link.getCourseUnit().getCode());
            if (record != null) {
                evidence.computeIfAbsent(link.getCompetency().getCode(), code -> new ArrayList<>())
                        .add(toEvidence(record, link));
            }
        }
        return evidence;
    }

    private static Evidence toEvidence(AcademicRecord record, CourseUnitCompetency link) {
        CourseUnit unit = record.getCourseUnit();
        String label = unit.getCode() + " – " + unit.getName() + " · grade " + record.getGrade()
                + " (" + record.getAcademicYear() + ")";
        double weight = link.getWeight() * unit.getEcts() / STANDARD_ECTS;
        return new Evidence(EvidenceType.ACADEMIC_RESULT, label, record.getGrade() / 20.0, weight);
    }
}
