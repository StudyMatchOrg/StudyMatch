package pt.upt.studymatch.dto;

import java.time.LocalDateTime;
import java.util.List;
import pt.upt.studymatch.domain.AcademicEvidence;
import pt.upt.studymatch.domain.AggregatedScore;
import pt.upt.studymatch.domain.ColdStartPolicy;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.EvidenceAggregator;
import pt.upt.studymatch.domain.EvidenceType;
import pt.upt.studymatch.domain.ProfileStatus;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.StudentProfile;

/**
 * A student's profile with, for each competency, the score and the evidence behind it, so the
 * frontend can explain every number.
 */
public record ProfileResponse(Long studentId, String studentName, LocalDateTime calculatedAt, Rules rules,
        List<CompetencyScoreResponse> competencies) {

    /** The constants of the profile rule, sent so the frontend does not hard-code them. */
    public record Rules(double standardEcts, double teacherAssessmentWeight, double confidenceConstant) {
    }

    public record CompetencyScoreResponse(CompetencyResponse competency, Double score, double confidence,
            ProfileStatus status, double totalWeight, List<EvidenceResponse> evidence) {
    }

    public record EvidenceResponse(EvidenceType type, String label, double value, double weight, double share) {
    }

    public static ProfileResponse from(Student student, StudentProfile profile, LocalDateTime calculatedAt) {
        Rules rules = new Rules(AcademicEvidence.STANDARD_ECTS, ColdStartPolicy.TEACHER_ASSESSMENT_WEIGHT,
                EvidenceAggregator.CONFIDENCE_CONSTANT);
        List<CompetencyScoreResponse> competencies = profile.scores().stream()
                .map(s -> toResponse(s.competency(), s.score()))
                .toList();
        return new ProfileResponse(student.getId(), student.getName(), calculatedAt, rules, competencies);
    }

    private static CompetencyScoreResponse toResponse(Competency competency, AggregatedScore score) {
        List<EvidenceResponse> evidence = score.evidence().stream()
                .map(c -> new EvidenceResponse(c.evidence().type(), c.evidence().label(), c.evidence().value(),
                        c.evidence().weight(), c.share()))
                .toList();
        return new CompetencyScoreResponse(CompetencyResponse.from(competency), score.score(), score.confidence(),
                score.status(), score.totalWeight(), evidence);
    }
}
