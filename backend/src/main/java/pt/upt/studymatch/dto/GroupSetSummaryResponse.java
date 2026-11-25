package pt.upt.studymatch.dto;

import java.time.LocalDateTime;
import java.util.List;
import pt.upt.studymatch.domain.GroupSet;

/** A generation in the history of a project. */
public record GroupSetSummaryResponse(Long id, LocalDateTime generatedAt, String generatedBy, String strategy,
        int groupSize, int groupCount, int studentCount, double weakestScore, double averageScore,
        double randomWeakestScore, List<String> warnings) {

    public static GroupSetSummaryResponse from(GroupSet set) {
        return new GroupSetSummaryResponse(set.getId(), set.getGeneratedAt(), set.getGeneratedBy().getDisplayName(),
                set.getStrategy(), set.getGroupSize(), set.getGroups().size(), set.studentCount(),
                set.getWeakestScore(), set.getAverageScore(), set.getRandomWeakestScore(), set.getWarnings());
    }
}
