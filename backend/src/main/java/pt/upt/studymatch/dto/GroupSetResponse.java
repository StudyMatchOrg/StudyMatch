package pt.upt.studymatch.dto;

import java.time.LocalDateTime;
import java.util.List;
import pt.upt.studymatch.domain.CompetencyStrength;
import pt.upt.studymatch.domain.GroupExplanation;
import pt.upt.studymatch.domain.GroupMember;
import pt.upt.studymatch.domain.GroupSet;
import pt.upt.studymatch.domain.StudentGroup;

/**
 * A generation of groups with everything needed to understand it: how balanced it is compared
 * with random groups, and, for each group, its members and why they work together.
 */
public record GroupSetResponse(Long id, Long projectId, String projectName, LocalDateTime generatedAt,
        String generatedBy, String strategy, long seed, int groupSize, double weakestScore, double averageScore,
        double spread, double randomWeakestScore, List<String> warnings, List<GroupResponse> groups) {

    public record GroupResponse(int number, double score, List<MemberResponse> members,
            List<CompetencyStrength> competencies, List<String> strengths, List<String> gaps) {
    }

    public record MemberResponse(Long studentId, String studentNumber, String name, double overallScore,
            boolean lowEvidence, String strongestCompetency) {
    }

    public static GroupSetResponse from(GroupSet set) {
        List<GroupResponse> groups = set.getGroups().stream().map(GroupSetResponse::toResponse).toList();
        return new GroupSetResponse(set.getId(), set.getContext().getId(), set.getContext().getName(),
                set.getGeneratedAt(), set.getGeneratedBy().getDisplayName(), set.getStrategy(), set.getSeed(),
                set.getGroupSize(), set.getWeakestScore(), set.getAverageScore(), set.getSpread(),
                set.getRandomWeakestScore(), set.getWarnings(), groups);
    }

    private static GroupResponse toResponse(StudentGroup group) {
        List<MemberResponse> members = group.getMembers().stream().map(GroupSetResponse::toResponse).toList();
        List<CompetencyStrength> competencies = group.getStrengths();
        return new GroupResponse(group.getNumber(), group.getScore(), members, competencies,
                GroupExplanation.strengths(competencies), GroupExplanation.gaps(competencies));
    }

    private static MemberResponse toResponse(GroupMember member) {
        return new MemberResponse(member.getStudent().getId(), member.getStudent().getStudentNumber(),
                member.getStudent().getName(), member.getOverallScore(), member.isLowEvidence(),
                member.getStrongestCompetency());
    }
}
