package pt.upt.studymatch.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.CourseUnitRequest;
import pt.upt.studymatch.dto.CourseUnitResponse;
import pt.upt.studymatch.dto.DegreeRequest;
import pt.upt.studymatch.dto.DegreeResponse;
import pt.upt.studymatch.service.AcademicStructureService;

@RestController
@RequestMapping("/api")
public class AcademicStructureController {

    private final AcademicStructureService academicStructureService;

    public AcademicStructureController(AcademicStructureService academicStructureService) {
        this.academicStructureService = academicStructureService;
    }

    @GetMapping("/degrees")
    public List<DegreeResponse> findDegrees() {
        return academicStructureService.findDegrees();
    }

    @GetMapping("/course-units")
    public List<CourseUnitResponse> findCourseUnits(@RequestParam(required = false) Long degreeId) {
        return academicStructureService.findCourseUnits(degreeId);
    }

    // Admin only (see SecurityConfig)
    @PostMapping("/degrees")
    @ResponseStatus(HttpStatus.CREATED)
    public DegreeResponse createDegree(@Valid @RequestBody DegreeRequest request) {
        return academicStructureService.createDegree(request);
    }

    // Admin only (see SecurityConfig)
    @PostMapping("/course-units")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseUnitResponse createCourseUnit(@Valid @RequestBody CourseUnitRequest request) {
        return academicStructureService.createCourseUnit(request);
    }
}
