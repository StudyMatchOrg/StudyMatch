package pt.upt.studymatch.dto;

import java.util.List;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Degree;

public record CourseUnitResponse(
        Long id, String code, String name, double ects, int curricularYear, int semester,
        List<String> degreeCodes) {

    public static CourseUnitResponse from(CourseUnit unit) {
        List<String> degreeCodes = unit.getDegrees().stream().map(Degree::getCode).sorted().toList();
        return new CourseUnitResponse(unit.getId(), unit.getCode(), unit.getName(), unit.getEcts(),
                unit.getCurricularYear(), unit.getSemester(), degreeCodes);
    }
}
