package pt.upt.studymatch.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.CompetencyResponse;
import pt.upt.studymatch.dto.CourseUnitCompetencyResponse;
import pt.upt.studymatch.service.CompetencyService;

@RestController
@RequestMapping("/api")
public class CompetencyController {

    private final CompetencyService competencyService;

    public CompetencyController(CompetencyService competencyService) {
        this.competencyService = competencyService;
    }

    @GetMapping("/competencies")
    public List<CompetencyResponse> findCompetencies() {
        return competencyService.findCompetencies();
    }

    @GetMapping("/course-unit-competencies")
    public List<CourseUnitCompetencyResponse> findWeights(@RequestParam(required = false) Long courseUnitId) {
        return competencyService.findWeights(courseUnitId);
    }
}
