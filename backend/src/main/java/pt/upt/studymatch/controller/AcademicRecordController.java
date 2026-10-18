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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.AcademicRecordChange;
import pt.upt.studymatch.dto.AcademicRecordRequest;
import pt.upt.studymatch.dto.AcademicRecordResponse;
import pt.upt.studymatch.service.AcademicRecordService;

@RestController
@RequestMapping("/api")
public class AcademicRecordController {

    private final AcademicRecordService recordService;

    public AcademicRecordController(AcademicRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/students/{studentId}/academic-records")
    public List<AcademicRecordResponse> findByStudent(@PathVariable Long studentId) {
        return recordService.findByStudent(studentId);
    }

    @PostMapping("/students/{studentId}/academic-records")
    @ResponseStatus(HttpStatus.CREATED)
    public AcademicRecordResponse add(@PathVariable Long studentId, @Valid @RequestBody AcademicRecordRequest request) {
        return recordService.add(studentId, request);
    }

    @PutMapping("/academic-records/{id}")
    public AcademicRecordResponse change(@PathVariable Long id, @Valid @RequestBody AcademicRecordChange change) {
        return recordService.change(id, change);
    }

    @DeleteMapping("/academic-records/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        recordService.delete(id);
    }
}
