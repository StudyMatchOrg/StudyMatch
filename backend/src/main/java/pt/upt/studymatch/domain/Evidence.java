package pt.upt.studymatch.domain;

/**
 * One piece of evidence about a student's level in a competency.
 *
 * @param type   where the evidence comes from
 * @param label  human-readable description, shown when explaining the profile
 * @param value  the level the evidence indicates, from 0 to 1
 * @param weight how much the evidence counts; one standard 6-ECTS course unit fully about the
 *               competency has weight 1
 */
public record Evidence(EvidenceType type, String label, double value, double weight) {

    public Evidence {
        if (type == null) {
            throw new IllegalArgumentException("Evidence type is required");
        }
        if (Double.isNaN(value) || value < 0 || value > 1) {
            throw new IllegalArgumentException("Evidence value must be between 0 and 1, but was " + value);
        }
        if (!(weight > 0) || Double.isInfinite(weight)) {
            throw new IllegalArgumentException("Evidence weight must be greater than 0, but was " + weight);
        }
    }
}
