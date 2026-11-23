package pt.upt.studymatch.service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.AggregatedScore;
import pt.upt.studymatch.domain.ProfiledStudent;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.StudentProfile;
import pt.upt.studymatch.dto.StudentSearchFilter;
import pt.upt.studymatch.dto.StudentSearchResponse;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.StudentRepository;

/**
 * Searching and filtering students (UC16, team feature). The filters on the student's own data
 * are applied first; profiles are only calculated when a filter or the sort order needs them.
 */
@Service
public class StudentSearchService {

    private static final Set<String> SORTS = Set.of("number", "name", "score");

    private final StudentRepository studentRepository;
    private final AcademicRecordRepository recordRepository;
    private final CompetencyRepository competencyRepository;
    private final ProfileService profileService;

    public StudentSearchService(StudentRepository studentRepository, AcademicRecordRepository recordRepository,
            CompetencyRepository competencyRepository, ProfileService profileService) {
        this.studentRepository = studentRepository;
        this.recordRepository = recordRepository;
        this.competencyRepository = competencyRepository;
        this.profileService = profileService;
    }

    @Transactional(readOnly = true)
    public List<StudentSearchResponse> search(StudentSearchFilter filter) {
        String sort = validate(filter);
        Stream<Student> students = studentRepository.findAll(Sort.by("studentNumber")).stream();
        if (hasText(filter.q())) {
            String query = normalise(filter.q());
            students = students.filter(s -> normalise(s.getName()).contains(query) || s.getStudentNumber().contains(query));
        }
        if (filter.degreeId() != null) {
            students = students.filter(s -> s.getDegree().getId().equals(filter.degreeId()));
        }
        if (filter.enrolmentYear() != null) {
            students = students.filter(s -> s.getEnrolmentYear() == filter.enrolmentYear());
        }
        if (filter.courseUnitId() != null) {
            Set<Long> enrolled = recordRepository.findStudentIdsEnrolledIn(filter.courseUnitId());
            students = students.filter(s -> enrolled.contains(s.getId()));
        }
        List<Student> matching = students.toList();
        if (!filter.needsProfiles() && !sort.equals("score")) {
            return matching.stream()
                    .sorted(sort.equals("name") ? Comparator.comparing(Student::getName) : (a, b) -> 0)
                    .map(s -> StudentSearchResponse.from(s, null, null))
                    .toList();
        }
        return withProfiles(matching, filter, sort);
    }

    private List<StudentSearchResponse> withProfiles(List<Student> students, StudentSearchFilter filter, String sort) {
        Stream<Result> results = profileService.profilesOf(students).stream().map(p -> result(p, filter.competency()));
        if (filter.minScore() != null) {
            results = results.filter(r -> r.score() != null && r.score() >= filter.minScore());
        }
        if (Boolean.TRUE.equals(filter.lowEvidence())) {
            results = results.filter(Result::lowEvidence);
        }
        Comparator<Result> order = switch (sort) {
            case "name" -> Comparator.comparing(r -> r.student().getName());
            case "score" -> Comparator.comparing(Result::score, Comparator.nullsLast(Comparator.reverseOrder()));
            default -> (a, b) -> 0;
        };
        return results.sorted(order)
                .map(r -> StudentSearchResponse.from(r.student(), r.score(), r.lowEvidence()))
                .toList();
    }

    private static Result result(ProfiledStudent profiled, String competency) {
        StudentProfile profile = profiled.profile();
        Double score = hasText(competency)
                ? profile.scoreOf(competency.trim()).map(AggregatedScore::score).orElse(null)
                : null;
        boolean lowEvidence = profile.scores().stream().noneMatch(s -> s.score().status().hasAcademicEvidence());
        return new Result(profiled.student(), score, lowEvidence);
    }

    private String validate(StudentSearchFilter filter) {
        String sort = hasText(filter.sort()) ? filter.sort().trim().toLowerCase(Locale.ROOT) : "number";
        if (!SORTS.contains(sort)) {
            throw new BusinessRuleException("Sort must be one of number, name or score, but was " + filter.sort());
        }
        if (hasText(filter.competency()) && competencyRepository.findByCode(filter.competency().trim()).isEmpty()) {
            throw new NotFoundException("Competency " + filter.competency() + " does not exist");
        }
        if ((filter.minScore() != null || sort.equals("score")) && !hasText(filter.competency())) {
            throw new BusinessRuleException("Choose a competency to filter or sort by score");
        }
        if (filter.minScore() != null && (filter.minScore() < 0 || filter.minScore() > 1)) {
            throw new BusinessRuleException("Minimum score must be between 0 and 1, but was " + filter.minScore());
        }
        return sort;
    }

    /** Lower case without accents, so "joao" finds "João". */
    static String normalise(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record Result(Student student, Double score, boolean lowEvidence) {
    }
}
