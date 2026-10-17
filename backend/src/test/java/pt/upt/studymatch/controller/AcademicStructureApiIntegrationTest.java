package pt.upt.studymatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AcademicStructureApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private CourseUnitRepository courseUnitRepository;

    private Degree lei;

    @BeforeEach
    void createAcademicStructure() {
        lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        Degree lig = degreeRepository.save(new Degree("LIG", "Informática de Gestão"));
        courseUnitRepository.save(new CourseUnit("POO", "Programação Orientada a Objetos", 7.5, 1, 2, Set.of(lei, lig)));
        courseUnitRepository.save(new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lei)));
        courseUnitRepository.save(new CourseUnit("SI", "Sistemas de Informação", 6, 2, 2, Set.of(lig)));
    }

    @Test
    void degreesAreListedByCode() throws Exception {
        mockMvc.perform(get("/api/degrees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("LEI"))
                .andExpect(jsonPath("$[1].code").value("LIG"));
    }

    @Test
    void courseUnitsOfADegreeAreListedInCurricularOrder() throws Exception {
        mockMvc.perform(get("/api/course-units").param("degreeId", lei.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("IP"))
                .andExpect(jsonPath("$[1].code").value("POO"))
                .andExpect(jsonPath("$[1].ects").value(7.5))
                .andExpect(jsonPath("$[1].degreeCodes.length()").value(2));
    }

    @Test
    void unknownDegreeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/course-units").param("degreeId", "9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Degree 9999 does not exist"));
    }

    @Test
    void nonNumericDegreeIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/course-units").param("degreeId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for degreeId"));
    }
}
