package pt.upt.studymatch.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.CourseUnitResponse;
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
}
