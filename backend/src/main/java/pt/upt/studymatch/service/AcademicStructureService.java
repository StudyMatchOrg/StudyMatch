package pt.upt.studymatch.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.dto.CourseUnitRequest;
import pt.upt.studymatch.dto.CourseUnitResponse;
import pt.upt.studymatch.dto.DegreeRequest;
import pt.upt.studymatch.dto.DegreeResponse;
import pt.upt.studymatch.exception.ConflictException;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;

// Degrees and course units: read by everyone, created by admins (UC3 to UC6)
@Service
public class AcademicStructureService {

    private final DegreeRepository degreeRepository;
    private final CourseUnitRepository courseUnitRepository;

    public AcademicStructureService(DegreeRepository degreeRepository, CourseUnitRepository courseUnitRepository) {
        this.degreeRepository = degreeRepository;
        this.courseUnitRepository = courseUnitRepository;
    }

    @Transactional(readOnly = true)
    public List<DegreeResponse> findDegrees() {
        return degreeRepository.findAllByOrderByCodeAsc().stream().map(DegreeResponse::from).toList();
    }

    @Transactional
    public DegreeResponse createDegree(DegreeRequest request) {
        String code = request.code().trim().toUpperCase();
        if (degreeRepository.existsByCode(code)) {
            throw new ConflictException("Degree code " + code + " already exists");
        }
        return DegreeResponse.from(degreeRepository.save(new Degree(code, request.name())));
    }

    @Transactional
    public CourseUnitResponse createCourseUnit(CourseUnitRequest request) {
        String code = request.code().trim().toUpperCase();
        if (courseUnitRepository.existsByCode(code)) {
            throw new ConflictException("Course unit code " + code + " already exists");
        }
        Set<Degree> degrees = new HashSet<>();
        for (Long degreeId : request.degreeIds()) {
            degrees.add(degreeRepository.findById(degreeId)
                    .orElseThrow(() -> new NotFoundException("Degree " + degreeId + " does not exist")));
        }
        CourseUnit unit = new CourseUnit(code, request.name().trim(), request.ects(), request.curricularYear(),
                request.semester(), degrees);
        return CourseUnitResponse.from(courseUnitRepository.save(unit));
    }

    @Transactional(readOnly = true)
    public List<CourseUnitResponse> findCourseUnits(Long degreeId) {
        if (degreeId == null) {
            return courseUnitRepository.findAllInCurricularOrder().stream().map(CourseUnitResponse::from).toList();
        }
        if (!degreeRepository.existsById(degreeId)) {
            throw new NotFoundException("Degree " + degreeId + " does not exist");
        }
        return courseUnitRepository.findByDegreeInCurricularOrder(degreeId).stream()
                .map(CourseUnitResponse::from)
                .toList();
    }
}
