package pt.upt.studymatch.dto;

import pt.upt.studymatch.domain.Competency;

public record CompetencyResponse(Long id, String code, String name, String description) {

    public static CompetencyResponse from(Competency competency) {
        return new CompetencyResponse(competency.getId(), competency.getCode(), competency.getName(),
                competency.getDescription());
    }
}
