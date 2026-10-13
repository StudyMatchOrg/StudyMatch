package pt.upt.studymatch.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.dto.StudentRequest;
import pt.upt.studymatch.dto.StudentResponse;
import pt.upt.studymatch.exception.DuplicateStudentException;
import pt.upt.studymatch.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll(Sort.by("studentNumber")).stream()
                .map(StudentResponse::from)
                .toList();
    }

    @Transactional
    public StudentResponse register(StudentRequest request) {
        String studentNumber = request.studentNumber().trim();
        String email = request.email().trim().toLowerCase();

        if (studentRepository.existsByStudentNumber(studentNumber)) {
            throw new DuplicateStudentException("Student number " + studentNumber + " already exists");
        }
        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateStudentException("Email " + email + " is already in use");
        }

        Student student = new Student(studentNumber, request.name().trim(), email);
        return StudentResponse.from(studentRepository.save(student));
    }
}
