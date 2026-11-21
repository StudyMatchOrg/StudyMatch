package pt.upt.studymatch.domain;

import java.util.Comparator;
import java.util.List;

/** Turns a group's competency strengths into what the teacher reads: its strengths and its gaps. */
public final class GroupExplanation {

    /** A relevant competency below this strength is shown as a gap of the group. */
    public static final double GAP_THRESHOLD = 0.5;

    private static final int STRENGTHS_SHOWN = 2;

    private GroupExplanation() {
    }

    /** The group's strongest competencies, strongest first. */
    public static List<String> strengths(List<CompetencyStrength> competencies) {
        return competencies.stream()
                .filter(c -> c.strength() >= GAP_THRESHOLD)
                .sorted(Comparator.comparingDouble(CompetencyStrength::strength).reversed())
                .limit(STRENGTHS_SHOWN)
                .map(CompetencyStrength::name)
                .toList();
    }

    /** Relevant competencies in which the group is weak, weakest first. */
    public static List<String> gaps(List<CompetencyStrength> competencies) {
        return competencies.stream()
                .filter(c -> c.strength() < GAP_THRESHOLD)
                .sorted(Comparator.comparingDouble(CompetencyStrength::strength))
                .map(CompetencyStrength::name)
                .toList();
    }
}
