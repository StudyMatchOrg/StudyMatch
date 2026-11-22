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
import pt.upt.studymatch.dto.CompetencyChange;
import pt.upt.studymatch.dto.CompetencyRequest;
import pt.upt.studymatch.dto.CompetencyResponse;
import pt.upt.studymatch.dto.CourseUnitCompetencyRequest;
import pt.upt.studymatch.dto.CourseUnitCompetencyResponse;
import pt.upt.studymatch.dto.WeightChange;
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

    @PostMapping("/competencies")
    @ResponseStatus(HttpStatus.CREATED)
    public CompetencyResponse create(@Valid @RequestBody CompetencyRequest request) {
        return competencyService.create(request);
    }

    @PutMapping("/competencies/{id}")
    public CompetencyResponse change(@PathVariable Long id, @Valid @RequestBody CompetencyChange change) {
        return competencyService.change(id, change);
    }

    @PostMapping("/course-unit-competencies")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseUnitCompetencyResponse link(@Valid @RequestBody CourseUnitCompetencyRequest request) {
        return competencyService.link(request);
    }

    @PutMapping("/course-unit-competencies/{id}")
    public CourseUnitCompetencyResponse changeWeight(@PathVariable Long id, @Valid @RequestBody WeightChange change) {
        return competencyService.changeWeight(id, change);
    }

    @DeleteMapping("/course-unit-competencies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlink(@PathVariable Long id) {
        competencyService.unlink(id);
    }
}
