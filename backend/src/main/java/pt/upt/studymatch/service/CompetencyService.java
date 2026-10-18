package pt.upt.studymatch.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.dto.CompetencyResponse;
import pt.upt.studymatch.dto.CourseUnitCompetencyResponse;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitCompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;

@Service
public class CompetencyService {

    private final CompetencyRepository competencyRepository;
    private final CourseUnitCompetencyRepository courseUnitCompetencyRepository;
    private final CourseUnitRepository courseUnitRepository;

    public CompetencyService(CompetencyRepository competencyRepository,
            CourseUnitCompetencyRepository courseUnitCompetencyRepository, CourseUnitRepository courseUnitRepository) {
        this.competencyRepository = competencyRepository;
        this.courseUnitCompetencyRepository = courseUnitCompetencyRepository;
        this.courseUnitRepository = courseUnitRepository;
    }

    @Transactional(readOnly = true)
    public List<CompetencyResponse> findCompetencies() {
        return competencyRepository.findAllByOrderByNameAsc().stream().map(CompetencyResponse::from).toList();
    }

    /** All course-unit weights, or only those of one course unit. */
    @Transactional(readOnly = true)
    public List<CourseUnitCompetencyResponse> findWeights(Long courseUnitId) {
        if (courseUnitId == null) {
            return courseUnitCompetencyRepository.findAllInCurricularOrder().stream()
                    .map(CourseUnitCompetencyResponse::from)
                    .toList();
        }
        if (!courseUnitRepository.existsById(courseUnitId)) {
            throw new NotFoundException("Course unit " + courseUnitId + " does not exist");
        }
        return courseUnitCompetencyRepository.findByCourseUnit(courseUnitId).stream()
                .map(CourseUnitCompetencyResponse::from)
                .toList();
    }
}
