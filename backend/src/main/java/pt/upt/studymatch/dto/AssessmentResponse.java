package pt.upt.studymatch.dto;

import java.time.LocalDate;
import pt.upt.studymatch.domain.CompetencyAssessment;

public record AssessmentResponse(CompetencyResponse competency, double value, LocalDate assessedOn) {

    public static AssessmentResponse from(CompetencyAssessment assessment) {
        return new AssessmentResponse(CompetencyResponse.from(assessment.getCompetency()), assessment.getValue(),
                assessment.getAssessedOn());
    }
}
