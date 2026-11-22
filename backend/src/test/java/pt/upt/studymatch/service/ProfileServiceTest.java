package pt.upt.studymatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.AcademicRecord;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CompetencyAssessment;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.CourseUnitCompetency;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.ProfileStatus;
import pt.upt.studymatch.domain.ProfiledStudent;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyAssessmentRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitCompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.StudentRepository;

/** Loading many profiles at once gives the same profiles as loading them one by one. */
@SpringBootTest
@Transactional
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private CourseUnitRepository courseUnitRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private CourseUnitCompetencyRepository linkRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicRecordRepository recordRepository;

    @Autowired
    private CompetencyAssessmentRepository assessmentRepository;

    private Student ana;
    private Student bruno;
    private Student carla;

    @BeforeEach
    void createData() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        CourseUnit ip = courseUnitRepository.save(new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lei)));
        Competency programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        linkRepository.save(new CourseUnitCompetency(ip, programming, 1.0));
        ana = studentRepository.save(new Student("52031", "Ana Sousa", "52031@alunos.upt.pt", lei, 2025));
        bruno = studentRepository.save(new Student("53007", "Bruno Costa", "53007@alunos.upt.pt", lei, 2026));
        carla = studentRepository.save(new Student("53015", "Carla Mendes", "53015@alunos.upt.pt", lei, 2026));
        recordRepository.save(new AcademicRecord(ana, ip, "2025/2026", 1, RecordStatus.PASSED, 17));
        assessmentRepository.save(new CompetencyAssessment(bruno, programming, 0.6, LocalDate.of(2026, 10, 9)));
    }

    @Test
    void profilesOfManyStudentsKeepTheirOrderAndMatchTheSingleProfiles() {
        List<ProfiledStudent> profiles = profileService.profilesOf(List.of(carla, ana, bruno));

        assertThat(profiles).extracting(p -> p.student().getName())
                .containsExactly("Carla Mendes", "Ana Sousa", "Bruno Costa");
        assertThat(profiles.get(0).profile().scoreOf("PROG").orElseThrow().status()).isEqualTo(ProfileStatus.UNKNOWN);
        assertThat(profiles.get(1).profile().scoreOf("PROG").orElseThrow().score()).isCloseTo(0.85, within(0.001));
        assertThat(profiles.get(2).profile().scoreOf("PROG").orElseThrow().status())
                .isEqualTo(ProfileStatus.INITIAL_ONLY);
        assertThat(profileService.profileOf(ana.getId()).competencies().getFirst().score())
                .isCloseTo(profiles.get(1).profile().scoreOf("PROG").orElseThrow().score(), within(0.0001));
    }

    @Test
    void noStudentsGiveNoProfiles() {
        assertThat(profileService.profilesOf(List.of())).isEmpty();
    }
}
