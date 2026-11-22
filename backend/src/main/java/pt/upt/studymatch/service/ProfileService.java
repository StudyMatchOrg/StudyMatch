package pt.upt.studymatch.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.AcademicRecord;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CompetencyAssessment;
import pt.upt.studymatch.domain.CourseUnitCompetency;
import pt.upt.studymatch.domain.ProfileCalculator;
import pt.upt.studymatch.domain.ProfiledStudent;
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

    /**
     * The profiles of many students at once, in the given order. Loads all the evidence in four
     * queries instead of four per student, for search filters and group formation.
     */
    @Transactional(readOnly = true)
    public List<ProfiledStudent> profilesOf(List<Student> students) {
        if (students.isEmpty()) {
            return List.of();
        }
        List<Long> ids = students.stream().map(Student::getId).toList();
        List<Competency> competencies = competencyRepository.findAll();
        List<CourseUnitCompetency> links = linkRepository.findAllInCurricularOrder();
        Map<Long, List<AcademicRecord>> records = recordRepository.findByStudentsInOrder(ids).stream()
                .collect(Collectors.groupingBy(r -> r.getStudent().getId()));
        Map<Long, List<CompetencyAssessment>> assessments = assessmentRepository.findByStudents(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));
        return students.stream()
                .map(student -> new ProfiledStudent(student, calculator.calculate(competencies,
                        records.getOrDefault(student.getId(), List.of()), links,
                        assessments.getOrDefault(student.getId(), List.of()))))
                .toList();
    }
}
