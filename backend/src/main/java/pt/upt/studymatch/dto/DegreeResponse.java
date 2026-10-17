package pt.upt.studymatch.dto;

import pt.upt.studymatch.domain.Degree;

public record DegreeResponse(Long id, String code, String name) {

    public static DegreeResponse from(Degree degree) {
        return new DegreeResponse(degree.getId(), degree.getCode(), degree.getName());
    }
}
