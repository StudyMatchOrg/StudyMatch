package pt.upt.studymatch.dto;

import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.CourseUnitCompetency;

public record CourseUnitCompetencyResponse(
        Long id, Long courseUnitId, String courseUnitCode, String courseUnitName, double ects,
        CompetencyResponse competency, double weight) {

    public static CourseUnitCompetencyResponse from(CourseUnitCompetency link) {
        CourseUnit unit = link.getCourseUnit();
        return new CourseUnitCompetencyResponse(link.getId(), unit.getId(), unit.getCode(), unit.getName(), unit.getEcts(),
                CompetencyResponse.from(link.getCompetency()), link.getWeight());
    }
}
