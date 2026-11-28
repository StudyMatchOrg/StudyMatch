package pt.upt.studymatch.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.StudentRequest;
import pt.upt.studymatch.dto.StudentResponse;
import pt.upt.studymatch.dto.StudentSearchFilter;
import pt.upt.studymatch.dto.StudentSearchResponse;
import pt.upt.studymatch.service.StudentDeletionService;
import pt.upt.studymatch.service.StudentSearchService;
import pt.upt.studymatch.service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final StudentSearchService searchService;
    private final StudentDeletionService deletionService;

    public StudentController(StudentService studentService, StudentSearchService searchService,
            StudentDeletionService deletionService) {
        this.studentService = studentService;
        this.searchService = searchService;
        this.deletionService = deletionService;
    }

    // Without parameters, lists every student by number (as in Sprint 2).
    @GetMapping
    public List<StudentSearchResponse> search(@RequestParam(required = false) String q,
            @RequestParam(required = false) Long degreeId, @RequestParam(required = false) Integer enrolmentYear,
            @RequestParam(required = false) Long courseUnitId, @RequestParam(required = false) String competency,
            @RequestParam(required = false) Double minScore, @RequestParam(required = false) Boolean lowEvidence,
            @RequestParam(required = false) String sort) {
        return searchService.search(new StudentSearchFilter(q, degreeId, enrolmentYear, courseUnitId, competency,
                minScore, lowEvidence, sort));
    }

    @GetMapping("/{id}")
    public StudentResponse findById(@PathVariable Long id) {
        return studentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse register(@Valid @RequestBody StudentRequest request) {
        return studentService.register(request);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return studentService.update(id, request);
    }

    // Admin only (see SecurityConfig)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deletionService.delete(id);
    }
}
