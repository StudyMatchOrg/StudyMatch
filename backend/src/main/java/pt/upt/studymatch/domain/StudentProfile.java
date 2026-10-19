package pt.upt.studymatch.domain;

import java.util.List;
import java.util.Optional;

/**
 * A student's competency profile: one aggregated score per competency. It is always calculated
 * from the current evidence and never stored, so it cannot get out of date.
 */
public record StudentProfile(List<CompetencyScore> scores) {

    public record CompetencyScore(Competency competency, AggregatedScore score) {
    }

    public Optional<AggregatedScore> scoreOf(String competencyCode) {
        return scores.stream()
                .filter(s -> s.competency().getCode().equals(competencyCode))
                .map(CompetencyScore::score)
                .findFirst();
    }
}
