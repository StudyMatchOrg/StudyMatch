package pt.upt.studymatch.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.exception.ConflictException;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyAssessmentRepository;
import pt.upt.studymatch.repository.GroupMemberRepository;
import pt.upt.studymatch.repository.GroupingContextRepository;
import pt.upt.studymatch.repository.StudentRepository;

/**
 * Deleting students (UC9). A student who is in saved groups cannot be deleted, because generated
 * groups must stay consultable as they were (brief section 3.1). Otherwise the student's academic
 * records, initial assessments and project memberships are removed with them.
 */
@Service
public class StudentDeletionService {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final AcademicRecordRepository recordRepository;
    private final CompetencyAssessmentRepository assessmentRepository;
    private final GroupingContextRepository contextRepository;
    private final GroupMemberRepository memberRepository;

    public StudentDeletionService(StudentService studentService, StudentRepository studentRepository,
            AcademicRecordRepository recordRepository, CompetencyAssessmentRepository assessmentRepository,
            GroupingContextRepository contextRepository, GroupMemberRepository memberRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.recordRepository = recordRepository;
        this.assessmentRepository = assessmentRepository;
        this.contextRepository = contextRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void delete(Long id) {
        Student student = studentService.requireStudent(id);
        if (memberRepository.existsByStudentId(id)) {
            throw new ConflictException("Student " + student.getStudentNumber()
                    + " is in saved groups and cannot be deleted, so that earlier groups stay complete");
        }
        contextRepository.findByStudentsId(id).forEach(context -> context.removeStudent(student));
        recordRepository.deleteByStudentId(id);
        assessmentRepository.deleteByStudentId(id);
        studentRepository.delete(student);
    }
}
