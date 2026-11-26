package pt.upt.studymatch.dto;

/**
 * The search box and filters of the Students page (UC16). Every field is optional; the filters
 * that are given must all hold.
 *
 * @param q             part of the name (ignoring case and accents) or of the student number
 * @param courseUnitId  only students currently enrolled in this course unit
 * @param competency    competency code used by {@code minScore} and by sorting on {@code score}
 * @param minScore      minimum score (0 to 1) in {@code competency}
 * @param lowEvidence   only students without any grade yet (cold start), who need a teacher assessment
 * @param sort          {@code number} (default), {@code name} or {@code score}
 */
public record StudentSearchFilter(String q, Long degreeId, Integer enrolmentYear, Long courseUnitId,
        String competency, Double minScore, Boolean lowEvidence, String sort) {

    public boolean needsProfiles() {
        return hasText(competency) || minScore != null || Boolean.TRUE.equals(lowEvidence);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
