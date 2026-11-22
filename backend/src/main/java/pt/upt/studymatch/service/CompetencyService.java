package pt.upt.studymatch.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.CourseUnitCompetency;
import pt.upt.studymatch.dto.CompetencyChange;
import pt.upt.studymatch.dto.CompetencyRequest;
import pt.upt.studymatch.dto.CompetencyResponse;
import pt.upt.studymatch.dto.CourseUnitCompetencyRequest;
import pt.upt.studymatch.dto.CourseUnitCompetencyResponse;
import pt.upt.studymatch.dto.WeightChange;
import pt.upt.studymatch.exception.ConflictException;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitCompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;

/** Competencies and how much each course unit develops them (UC11). */
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

    @Transactional
    public CompetencyResponse create(CompetencyRequest request) {
        String code = request.code().trim().toUpperCase();
        if (competencyRepository.existsByCode(code)) {
            throw new ConflictException("Competency code " + code + " already exists");
        }
        return CompetencyResponse.from(competencyRepository.save(
                new Competency(code, request.name(), request.description())));
    }

    @Transactional
    public CompetencyResponse change(Long id, CompetencyChange change) {
        Competency competency = requireCompetency(id);
        competency.change(change.name(), change.description());
        return CompetencyResponse.from(competency);
    }

    @Transactional
    public CourseUnitCompetencyResponse link(CourseUnitCompetencyRequest request) {
        CourseUnit unit = courseUnitRepository.findById(request.courseUnitId())
                .orElseThrow(() -> new NotFoundException("Course unit " + request.courseUnitId() + " does not exist"));
        Competency competency = requireCompetency(request.competencyId());
        if (courseUnitCompetencyRepository.existsByCourseUnitIdAndCompetencyId(unit.getId(), competency.getId())) {
            throw new ConflictException(unit.getCode() + " is already linked to " + competency.getName());
        }
        return CourseUnitCompetencyResponse.from(courseUnitCompetencyRepository.save(
                new CourseUnitCompetency(unit, competency, request.weight())));
    }

    @Transactional
    public CourseUnitCompetencyResponse changeWeight(Long linkId, WeightChange change) {
        CourseUnitCompetency link = requireLink(linkId);
        link.changeWeight(change.weight());
        return CourseUnitCompetencyResponse.from(link);
    }

    @Transactional
    public void unlink(Long linkId) {
        courseUnitCompetencyRepository.delete(requireLink(linkId));
    }

    private Competency requireCompetency(Long id) {
        return competencyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Competency " + id + " does not exist"));
    }

    private CourseUnitCompetency requireLink(Long id) {
        return courseUnitCompetencyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course unit weight " + id + " does not exist"));
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
