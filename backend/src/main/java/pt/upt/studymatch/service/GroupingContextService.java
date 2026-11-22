package pt.upt.studymatch.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.GroupingContext;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.dto.GroupingContextRequest;
import pt.upt.studymatch.dto.GroupingContextResponse;
import pt.upt.studymatch.dto.GroupingContextSummaryResponse;
import pt.upt.studymatch.dto.StudentResponse;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.GroupingContextRepository;
import pt.upt.studymatch.security.CurrentUser;

/**
 * Projects (grouping contexts, UC14): where groups are formed, with which students, group size and
 * competencies. A teacher only sees their own projects; an admin sees all of them.
 */
@Service
public class GroupingContextService {

    private final GroupingContextRepository contextRepository;
    private final CourseUnitRepository courseUnitRepository;
    private final CompetencyRepository competencyRepository;
    private final StudentService studentService;
    private final CurrentUser currentUser;

    public GroupingContextService(GroupingContextRepository contextRepository,
            CourseUnitRepository courseUnitRepository, CompetencyRepository competencyRepository,
            StudentService studentService, CurrentUser currentUser) {
        this.contextRepository = contextRepository;
        this.courseUnitRepository = courseUnitRepository;
        this.competencyRepository = competencyRepository;
        this.studentService = studentService;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<GroupingContextSummaryResponse> findAll() {
        UserAccount account = currentUser.require();
        List<GroupingContext> contexts = account.isAdmin()
                ? contextRepository.findAllByOrderByCreatedAtDesc()
                : contextRepository.findByOwnerIdOrderByCreatedAtDesc(account.getId());
        return contexts.stream().map(GroupingContextSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public GroupingContextResponse findById(Long id) {
        return GroupingContextResponse.from(requireVisible(id));
    }

    /**
     * Returns the project if the current user may see it. A project of another teacher is
     * reported as not existing, so its existence is not revealed.
     */
    @Transactional(readOnly = true)
    public GroupingContext requireVisible(Long id) {
        GroupingContext context = contextRepository.findById(id).orElse(null);
        if (context == null || !context.isVisibleTo(currentUser.require())) {
            throw new NotFoundException("Project " + id + " does not exist");
        }
        return context;
    }

    @Transactional
    public GroupingContextResponse create(GroupingContextRequest request) {
        GroupingContext context = new GroupingContext(request.name(), request.description(),
                courseUnit(request.courseUnitId()), request.academicYear(), request.groupSize(), currentUser.require(),
                LocalDateTime.now());
        fill(context, request);
        return GroupingContextResponse.from(contextRepository.save(context));
    }

    @Transactional
    public GroupingContextResponse update(Long id, GroupingContextRequest request) {
        GroupingContext context = requireVisible(id);
        context.change(request.name(), request.description(), courseUnit(request.courseUnitId()),
                request.academicYear(), request.groupSize());
        fill(context, request);
        return GroupingContextResponse.from(context);
    }

    /** Students enrolled in a course unit in an academic year, to fill in a project's students. */
    @Transactional(readOnly = true)
    public List<StudentResponse> candidates(Long courseUnitId, String academicYear) {
        courseUnit(courseUnitId);
        return contextRepository.findEnrolledStudents(courseUnitId, academicYear).stream()
                .map(StudentResponse::from)
                .toList();
    }

    private void fill(GroupingContext context, GroupingContextRequest request) {
        List<Student> students = request.studentIds().stream().map(studentService::requireStudent).toList();
        context.replaceStudents(students);
        Map<Competency, Double> importance = new LinkedHashMap<>();
        for (GroupingContextRequest.CompetencyImportance item : request.competencies()) {
            Competency competency = competencyRepository.findById(item.competencyId())
                    .orElseThrow(() -> new NotFoundException("Competency " + item.competencyId() + " does not exist"));
            if (importance.put(competency, item.importance()) != null) {
                throw new BusinessRuleException("Competency " + competency.getName() + " is listed twice");
            }
        }
        context.replaceCompetencies(importance);
    }

    private CourseUnit courseUnit(Long id) {
        if (id == null) {
            return null;
        }
        return courseUnitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course unit " + id + " does not exist"));
    }
}
