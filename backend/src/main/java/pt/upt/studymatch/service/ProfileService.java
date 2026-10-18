package pt.upt.studymatch.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.ProfileCalculator;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.StudentProfile;
import pt.upt.studymatch.dto.ProfileResponse;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyAssessmentRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitCompetencyRepository;

/**
 * Loads all evidence about a student and calculates the profile on request, so it always
 * reflects the latest grades and assessments.
 */
@Service
public class ProfileService {

    private final StudentService studentService;
    private final CompetencyRepository competencyRepository;
    private final AcademicRecordRepository recordRepository;
    private final CourseUnitCompetencyRepository linkRepository;
    private final CompetencyAssessmentRepository assessmentRepository;
    private final ProfileCalculator calculator = new ProfileCalculator();

    public ProfileService(StudentService studentService, CompetencyRepository competencyRepository,
            AcademicRecordRepository recordRepository, CourseUnitCompetencyRepository linkRepository,
            CompetencyAssessmentRepository assessmentRepository) {
        this.studentService = studentService;
        this.competencyRepository = competencyRepository;
        this.recordRepository = recordRepository;
        this.linkRepository = linkRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse profileOf(Long studentId) {
        Student student = studentService.requireStudent(studentId);
        StudentProfile profile = calculator.calculate(
                competencyRepository.findAll(),
                recordRepository.findByStudentInOrder(studentId),
                linkRepository.findAllInCurricularOrder(),
                assessmentRepository.findByStudent(studentId));
        return ProfileResponse.from(student, profile, LocalDateTime.now());
    }
}
