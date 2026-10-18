package pt.upt.studymatch.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.CourseUnitCompetency;
import pt.upt.studymatch.domain.Degree;

@DataJpaTest
class CourseUnitCompetencyRepositoryTest {

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private CourseUnitRepository courseUnitRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private CourseUnitCompetencyRepository linkRepository;

    private CourseUnit poo;
    private Competency programming;

    @BeforeEach
    void createData() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        poo = courseUnitRepository.save(new CourseUnit("POO", "Programação Orientada a Objetos", 7.5, 1, 2, Set.of(lei)));
        programming = competencyRepository.save(new Competency("PROG", "Programming", null));
    }

    @Test
    void weightsOfACourseUnitAreFound() {
        linkRepository.save(new CourseUnitCompetency(poo, programming, 1.0));

        assertThat(linkRepository.findByCourseUnit(poo.getId())).hasSize(1);
    }

    @Test
    void courseUnitCannotBeLinkedToTheSameCompetencyTwice() {
        linkRepository.saveAndFlush(new CourseUnitCompetency(poo, programming, 1.0));

        assertThatThrownBy(() -> linkRepository.saveAndFlush(new CourseUnitCompetency(poo, programming, 0.5)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
