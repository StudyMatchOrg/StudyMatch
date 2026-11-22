package pt.upt.studymatch.dto;

import java.time.LocalDateTime;
import java.util.List;
import pt.upt.studymatch.domain.GroupSizePlanner;
import pt.upt.studymatch.domain.GroupingContext;

/**
 * A project with its students and relevant competencies. {@code plannedSizes} previews the groups
 * that the current students and group size will give (e.g. 23 students, size 5 give 5, 5, 5, 4, 4).
 */
public record GroupingContextResponse(Long id, String name, String description, CourseUnitResponse courseUnit,
        String academicYear, int groupSize, String ownerName, LocalDateTime createdAt, List<StudentResponse> students,
        List<CompetencyImportanceResponse> competencies, List<Integer> plannedSizes, List<String> warnings) {

    public record CompetencyImportanceResponse(CompetencyResponse competency, double importance) {
    }

    public static GroupingContextResponse from(GroupingContext context) {
        List<StudentResponse> students = context.getStudents().stream().map(StudentResponse::from).toList();
        List<CompetencyImportanceResponse> competencies = context.getCompetencies().stream()
                .map(c -> new CompetencyImportanceResponse(CompetencyResponse.from(c.getCompetency()), c.getImportance()))
                .toList();
        List<Integer> plannedSizes = List.of();
        List<String> warnings;
        if (students.size() >= GroupSizePlanner.MIN_STUDENTS) {
            GroupSizePlanner.GroupSizePlan plan = new GroupSizePlanner().plan(students.size(), context.getGroupSize());
            plannedSizes = plan.sizes();
            warnings = plan.warnings();
        } else {
            warnings = List.of("Choose at least " + GroupSizePlanner.MIN_STUDENTS + " students to form groups.");
        }
        return new GroupingContextResponse(context.getId(), context.getName(), context.getDescription(),
                context.getCourseUnit() == null ? null : CourseUnitResponse.from(context.getCourseUnit()),
                context.getAcademicYear(), context.getGroupSize(), context.getOwner().getDisplayName(),
                context.getCreatedAt(), students, competencies, plannedSizes, warnings);
    }
}
