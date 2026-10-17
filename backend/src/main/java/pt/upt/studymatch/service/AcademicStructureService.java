package pt.upt.studymatch.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.dto.CourseUnitResponse;
import pt.upt.studymatch.dto.DegreeResponse;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;

// Provides read access to degrees and course units
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
