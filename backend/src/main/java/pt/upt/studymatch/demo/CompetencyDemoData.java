package pt.upt.studymatch.demo;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.CourseUnitCompetency;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitCompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;

/**
 * Demo competencies and the weight with which each demo course unit develops them.
 * Loaded only if no competencies exist yet.
 */
@Component
@Order(20)
@ConditionalOnProperty(name = "studymatch.demo-data.enabled", havingValue = "true")
public class CompetencyDemoData implements ApplicationRunner {

    private final CompetencyRepository competencyRepository;
    private final CourseUnitRepository courseUnitRepository;
    private final CourseUnitCompetencyRepository courseUnitCompetencyRepository;
    private final Map<String, Competency> competencies = new HashMap<>();

    public CompetencyDemoData(CompetencyRepository competencyRepository, CourseUnitRepository courseUnitRepository,
            CourseUnitCompetencyRepository courseUnitCompetencyRepository) {
        this.competencyRepository = competencyRepository;
        this.courseUnitRepository = courseUnitRepository;
        this.courseUnitCompetencyRepository = courseUnitCompetencyRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (competencyRepository.count() > 0) {
            return;
        }
        competency("PROG", "Programming", "Writing, structuring and debugging code.");
        competency("ALGO", "Algorithms", "Designing and analysing algorithms and data structures.");
        competency("MATH", "Mathematical Reasoning", "Logic, discrete mathematics and formal reasoning.");
        competency("PROB", "Problem Solving", "Breaking down open problems into solvable steps.");
        competency("DATA", "Databases", "Data modelling and querying relational databases.");
        competency("SWENG", "Software Engineering", "Requirements, design, processes and teamwork practices.");
        competency("QUAL", "Software Quality", "Testing, code review and quality assurance.");
        competency("NET", "Networking", "Computer networks and communication protocols.");

        weights("IP", "PROG", 1.0, "PROB", 0.6);
        weights("MD", "MATH", 1.0, "PROB", 0.5);
        weights("POO", "PROG", 1.0, "SWENG", 0.4);
        weights("AED", "ALGO", 1.0, "PROB", 0.8, "PROG", 0.5);
        weights("BD", "DATA", 1.0, "MATH", 0.2);
        weights("ES", "SWENG", 1.0, "QUAL", 0.4);
        weights("RC", "NET", 1.0);
        weights("SI", "SWENG", 0.6, "DATA", 0.5);
        weights("QS", "QUAL", 1.0, "SWENG", 0.6);
    }

    private void competency(String code, String name, String description) {
        competencies.put(code, competencyRepository.save(new Competency(code, name, description)));
    }

    /** Pairs of competency code and weight, e.g. {@code weights("IP", "PROG", 1.0, "PROB", 0.6)}. */
    private void weights(String courseUnitCode, Object... competencyWeights) {
        CourseUnit unit = courseUnitRepository.findByCode(courseUnitCode).orElseThrow();
        for (int i = 0; i < competencyWeights.length; i += 2) {
            Competency competency = competencies.get((String) competencyWeights[i]);
            double weight = (Double) competencyWeights[i + 1];
            courseUnitCompetencyRepository.save(new CourseUnitCompetency(unit, competency, weight));
        }
    }
}
