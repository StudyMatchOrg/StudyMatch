package pt.upt.studymatch.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.GroupingContext;
import pt.upt.studymatch.domain.Role;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.dto.GenerateGroupsRequest;
import pt.upt.studymatch.dto.GroupSetResponse;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.GroupSetRepository;
import pt.upt.studymatch.repository.GroupingContextRepository;
import pt.upt.studymatch.repository.StudentRepository;
import pt.upt.studymatch.repository.UserAccountRepository;

/**
 * Regression test for a defect found while testing the application: reading saved groups failed
 * with "could not initialize lazy collection" because the warnings were read after the
 * transaction had ended. The API tests did not see it because each runs inside one transaction,
 * so this test is deliberately not {@code @Transactional} and cleans up after itself.
 */
@SpringBootTest
@WithMockUser(username = "marta", roles = "TEACHER")
class GroupSetLoadingIntegrationTest {

    @Autowired
    private GroupGenerationService generationService;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserAccountRepository accountRepository;

    @Autowired
    private GroupingContextRepository contextRepository;

    @Autowired
    private GroupSetRepository groupSetRepository;

    private Long projectId;

    @BeforeEach
    void createProjectOfThreeStudents() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        Competency programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        UserAccount marta = accountRepository.save(new UserAccount("marta", "Prof. Marta", "hash", Role.TEACHER));
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            students.add(studentRepository.save(new Student("5300" + i, "Student " + i, "5300" + i + "@upt.pt", lei, 2026)));
        }
        GroupingContext project = new GroupingContext("Small project", null, null, null, 2, marta, LocalDateTime.now());
        project.replaceStudents(students);
        project.replaceCompetencies(Map.of(programming, 1.0));
        projectId = contextRepository.save(project).getId();
    }

    @AfterEach
    void removeData() {
        groupSetRepository.deleteAll();
        contextRepository.deleteAll();
        studentRepository.deleteAll();
        competencyRepository.deleteAll();
        accountRepository.deleteAll();
        degreeRepository.deleteAll();
    }

    @Test
    void savedGroupsCanBeReadOutsideATransaction() {
        Long id = generationService.generate(projectId, new GenerateGroupsRequest(null, null)).id();

        GroupSetResponse loaded = generationService.findById(id);

        assertThat(loaded.warnings()).singleElement().asString().contains("3 students");
        assertThat(loaded.groups()).singleElement().satisfies(group -> assertThat(group.members()).hasSize(3));
        assertThat(generationService.history(projectId)).singleElement()
                .satisfies(summary -> assertThat(summary.warnings()).hasSize(1));
    }
}
