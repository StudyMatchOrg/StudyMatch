package pt.upt.studymatch.service;

import java.time.Year;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.dto.StudentRequest;
import pt.upt.studymatch.dto.StudentResponse;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.exception.DuplicateStudentException;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final DegreeRepository degreeRepository;

    public StudentService(StudentRepository studentRepository, DegreeRepository degreeRepository) {
        this.studentRepository = studentRepository;
        this.degreeRepository = degreeRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll(Sort.by("studentNumber")).stream()
                .map(StudentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return StudentResponse.from(requireStudent(id));
    }

    // Returns the student with this id, or throws NotFoundException.
    // Also used by the records, assessments and profile services.
    @Transactional(readOnly = true)
    public Student requireStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student " + id + " does not exist"));
    }

    @Transactional
    public StudentResponse register(StudentRequest request) {
        StudentData data = validate(request);
        if (studentRepository.existsByStudentNumber(data.studentNumber())) {
            throw duplicateNumber(data.studentNumber());
        }
        if (studentRepository.existsByEmailIgnoreCase(data.email())) {
            throw duplicateEmail(data.email());
        }
        Student student = new Student(data.studentNumber(), data.name(), data.email(), data.degree(),
                request.enrolmentYear());
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = requireStudent(id);
        StudentData data = validate(request);
        // The student being edited may keep its own number and email.
        if (studentRepository.existsByStudentNumberAndIdNot(data.studentNumber(), id)) {
            throw duplicateNumber(data.studentNumber());
        }
        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(data.email(), id)) {
            throw duplicateEmail(data.email());
        }
        student.update(data.studentNumber(), data.name(), data.email(), data.degree(), request.enrolmentYear());
        return StudentResponse.from(student);
    }

    private StudentData validate(StudentRequest request) {
        int currentYear = Year.now().getValue();
        if (request.enrolmentYear() > currentYear) {
            throw new BusinessRuleException("Enrolment year cannot be after " + currentYear);
        }
        Degree degree = degreeRepository.findById(request.degreeId())
                .orElseThrow(() -> new BusinessRuleException("Degree " + request.degreeId() + " does not exist"));
        return new StudentData(request.studentNumber().trim(), request.name().trim(),
                request.email().trim().toLowerCase(), degree);
    }

    private static DuplicateStudentException duplicateNumber(String studentNumber) {
        return new DuplicateStudentException("Student number " + studentNumber + " already exists");
    }

    private static DuplicateStudentException duplicateEmail(String email) {
        return new DuplicateStudentException("Email " + email + " is already in use");
    }

    private record StudentData(String studentNumber, String name, String email, Degree degree) {
    }
}
