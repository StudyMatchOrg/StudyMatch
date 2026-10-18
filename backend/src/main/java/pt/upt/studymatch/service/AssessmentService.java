package pt.upt.studymatch.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CompetencyAssessment;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.dto.AssessmentResponse;
import pt.upt.studymatch.dto.AssessmentUpdateRequest;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.repository.CompetencyAssessmentRepository;
import pt.upt.studymatch.repository.CompetencyRepository;

@Service
public class AssessmentService {

    private final CompetencyAssessmentRepository assessmentRepository;
    private final CompetencyRepository competencyRepository;
    private final StudentService studentService;

    public AssessmentService(CompetencyAssessmentRepository assessmentRepository,
            CompetencyRepository competencyRepository, StudentService studentService) {
        this.assessmentRepository = assessmentRepository;
        this.competencyRepository = competencyRepository;
        this.studentService = studentService;
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> findByStudent(Long studentId) {
        studentService.requireStudent(studentId);
        return assessmentRepository.findByStudent(studentId).stream().map(AssessmentResponse::from).toList();
    }

    /**
     * Replaces the student's initial assessments with the given set. An assessment keeps its
     * date when its value does not change.
     */
    @Transactional
    public List<AssessmentResponse> replace(Long studentId, AssessmentUpdateRequest request) {
        Student student = studentService.requireStudent(studentId);
        Map<Long, Double> requested = requestedValues(request);
        Map<Long, CompetencyAssessment> current = assessmentRepository.findByStudent(studentId).stream()
                .collect(Collectors.toMap(a -> a.getCompetency().getId(), Function.identity()));
        LocalDate today = LocalDate.now();

        for (CompetencyAssessment assessment : current.values()) {
            if (!requested.containsKey(assessment.getCompetency().getId())) {
                assessmentRepository.delete(assessment);
            }
        }
        for (Map.Entry<Long, Double> entry : requested.entrySet()) {
            CompetencyAssessment existing = current.get(entry.getKey());
            if (existing == null) {
                Competency competency = competencyRepository.findById(entry.getKey())
                        .orElseThrow(() -> new BusinessRuleException("Competency " + entry.getKey() + " does not exist"));
                assessmentRepository.save(new CompetencyAssessment(student, competency, entry.getValue(), today));
            } else if (existing.getValue() != entry.getValue()) {
                existing.revise(entry.getValue(), today);
            }
        }
        assessmentRepository.flush();
        return findByStudent(studentId);
    }

    /** Competency id to value, without the entries that remove an assessment. */
    private static Map<Long, Double> requestedValues(AssessmentUpdateRequest request) {
        Set<Long> seen = new HashSet<>();
        Map<Long, Double> values = new HashMap<>();
        for (AssessmentUpdateRequest.Entry entry : request.assessments()) {
            if (!seen.add(entry.competencyId())) {
                throw new BusinessRuleException("Competency " + entry.competencyId() + " is assessed more than once");
            }
            if (entry.value() != null) {
                values.put(entry.competencyId(), entry.value());
            }
        }
        return values;
    }
}
