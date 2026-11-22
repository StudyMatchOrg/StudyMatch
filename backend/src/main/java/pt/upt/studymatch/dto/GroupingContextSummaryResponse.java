package pt.upt.studymatch.dto;

import java.time.LocalDateTime;
import pt.upt.studymatch.domain.GroupingContext;

/** A project in the list of projects. */
public record GroupingContextSummaryResponse(Long id, String name, String courseUnitCode, String academicYear,
        int groupSize, int studentCount, String ownerName, LocalDateTime createdAt) {

    public static GroupingContextSummaryResponse from(GroupingContext context) {
        return new GroupingContextSummaryResponse(context.getId(), context.getName(),
                context.getCourseUnit() == null ? null : context.getCourseUnit().getCode(), context.getAcademicYear(),
                context.getGroupSize(), context.getStudents().size(), context.getOwner().getDisplayName(),
                context.getCreatedAt());
    }
}
