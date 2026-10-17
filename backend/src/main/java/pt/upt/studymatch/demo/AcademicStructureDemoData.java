package pt.upt.studymatch.demo;

import java.util.Map;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;

// Adds sample courses on startup if enabled and none exist yet
@Component
@Order(10)
@ConditionalOnProperty(name = "studymatch.demo-data.enabled", havingValue = "true")
public class AcademicStructureDemoData implements ApplicationRunner {

    private final DegreeRepository degreeRepository;
    private final CourseUnitRepository courseUnitRepository;

    public AcademicStructureDemoData(DegreeRepository degreeRepository, CourseUnitRepository courseUnitRepository) {
        this.degreeRepository = degreeRepository;
        this.courseUnitRepository = courseUnitRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (degreeRepository.count() > 0) {
            return;
        }
        Degree lei = degreeRepository.save(new Degree("LEI", "Licenciatura em Engenharia Informática"));
        Degree lig = degreeRepository.save(new Degree("LIG", "Licenciatura em Informática de Gestão"));
        Map<String, Degree> degrees = Map.of("LEI", lei, "LIG", lig);

        unit("IP", "Introdução à Programação", 6.0, 1, 1, degrees, "LEI", "LIG");
        unit("MD", "Matemática Discreta", 6.0, 1, 1, degrees, "LEI");
        unit("POO", "Programação Orientada a Objetos", 7.5, 1, 2, degrees, "LEI", "LIG");
        unit("AED", "Algoritmos e Estruturas de Dados", 7.5, 2, 1, degrees, "LEI");
        unit("BD", "Bases de Dados", 6.0, 2, 1, degrees, "LEI", "LIG");
        unit("ES", "Engenharia de Software", 6.0, 2, 2, degrees, "LEI", "LIG");
        unit("RC", "Redes de Computadores", 6.0, 2, 2, degrees, "LEI");
        unit("SI", "Sistemas de Informação", 6.0, 2, 2, degrees, "LIG");
        unit("QS", "Qualidade de Software", 6.0, 3, 1, degrees, "LEI");
    }

    private void unit(String code, String name, double ects, int year, int semester,
            Map<String, Degree> degrees, String... degreeCodes) {
        Set<Degree> unitDegrees = java.util.Arrays.stream(degreeCodes).map(degrees::get)
                .collect(java.util.stream.Collectors.toSet());
        courseUnitRepository.save(new CourseUnit(code, name, ects, year, semester, unitDegrees));
    }
}
