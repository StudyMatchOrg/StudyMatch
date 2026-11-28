package pt.upt.studymatch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.GroupingContext;
import pt.upt.studymatch.domain.Role;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.GroupingContextRepository;
import pt.upt.studymatch.repository.StudentRepository;
import pt.upt.studymatch.repository.UserAccountRepository;

/** Exporting generated groups as CSV (UC7). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "marta", roles = "TEACHER")
class GroupExportApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Number groupSetId;

    @BeforeEach
    void generateGroups() throws Exception {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        Competency programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        UserAccount marta = accountRepository.save(new UserAccount("marta", "Prof. Marta", "hash", Role.TEACHER));
        List<Student> students = new ArrayList<>();
        String[] names = {"João Ribeiro", "Inês Carvalho", "Ana; Silva", "=HYPERLINK(\"x\")"};
        for (int i = 0; i < names.length; i++) {
            students.add(studentRepository.save(new Student("5204" + i, names[i], "5204" + i + "@upt.pt", lei, 2025)));
        }
        GroupingContext project = new GroupingContext("Projeto de Programação", null, null, null, 2, marta,
                LocalDateTime.now());
        project.replaceStudents(students);
        project.replaceCompetencies(Map.of(programming, 1.0));
        project = contextRepository.save(project);
        String result = mockMvc.perform(post("/api/grouping-contexts/" + project.getId() + "/group-sets")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        groupSetId = JsonPath.read(result, "$.id");
    }

    @Test
    void theCsvHasOneLinePerStudentAndOpensInExcel() throws Exception {
        byte[] body = mockMvc.perform(get("/api/group-sets/" + groupSetId + "/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"groups-projeto-de-programacao-" + groupSetId + ".csv\""))
                .andReturn().getResponse().getContentAsByteArray();
        String csv = new String(body, StandardCharsets.UTF_8);
        String[] lines = csv.substring(1).split("\r\n");

        assertThat(csv).startsWith("﻿");
        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("Project;Generated at;Group;Student number;Name;Email;Strongest competency");
        assertThat(csv).contains(";\"Ana; Silva\";", ";\"'=HYPERLINK(\"\"x\"\")\";", "João Ribeiro");
        assertThat(lines[1]).startsWith("Projeto de Programação;").contains(";1;");
    }

    @Test
    void anotherTeacherCannotExportTheGroups() throws Exception {
        accountRepository.save(new UserAccount("rui", "Prof. Rui", "hash", Role.TEACHER));

        mockMvc.perform(get("/api/group-sets/" + groupSetId + "/export").with(user("rui").roles("TEACHER")))
                .andExpect(status().isNotFound());
    }
}
