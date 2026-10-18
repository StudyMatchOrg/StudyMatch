package pt.upt.studymatch.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.AssessmentResponse;
import pt.upt.studymatch.dto.AssessmentUpdateRequest;
import pt.upt.studymatch.service.AssessmentService;

@RestController
@RequestMapping("/api/students/{studentId}/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public List<AssessmentResponse> findByStudent(@PathVariable Long studentId) {
        return assessmentService.findByStudent(studentId);
    }

    @PutMapping
    public List<AssessmentResponse> replace(@PathVariable Long studentId,
            @Valid @RequestBody AssessmentUpdateRequest request) {
        return assessmentService.replace(studentId, request);
    }
}
