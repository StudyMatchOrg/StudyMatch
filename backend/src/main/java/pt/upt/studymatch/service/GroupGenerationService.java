package pt.upt.studymatch.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.BalancedGroupingStrategy;
import pt.upt.studymatch.domain.GroupFormation;
import pt.upt.studymatch.domain.GroupSet;
import pt.upt.studymatch.domain.GroupingContext;
import pt.upt.studymatch.domain.GroupingStrategy;
import pt.upt.studymatch.domain.MatchingCriteria;
import pt.upt.studymatch.domain.MatchingProfileBuilder;
import pt.upt.studymatch.domain.MatchingStudent;
import pt.upt.studymatch.domain.RandomGroupingStrategy;
import pt.upt.studymatch.dto.GenerateGroupsRequest;
import pt.upt.studymatch.dto.GroupSetResponse;
import pt.upt.studymatch.dto.GroupSetSummaryResponse;
import pt.upt.studymatch.dto.GroupingStrategyResponse;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.GroupSetRepository;
import pt.upt.studymatch.security.CurrentUser;

/**
 * Generates the groups of a project (UC8) from the current student profiles, saves them, and lets
 * earlier generations be consulted.
 */
@Service
public class GroupGenerationService {

    private static final List<GroupingStrategy> STRATEGIES =
            List.of(new BalancedGroupingStrategy(), new RandomGroupingStrategy());

    private final GroupingContextService contextService;
    private final ProfileService profileService;
    private final GroupSetRepository groupSetRepository;
    private final CurrentUser currentUser;
    private final MatchingProfileBuilder profileBuilder = new MatchingProfileBuilder();
    private final GroupFormation formation = new GroupFormation();

    public GroupGenerationService(GroupingContextService contextService, ProfileService profileService,
            GroupSetRepository groupSetRepository, CurrentUser currentUser) {
        this.contextService = contextService;
        this.profileService = profileService;
        this.groupSetRepository = groupSetRepository;
        this.currentUser = currentUser;
    }

    public List<GroupingStrategyResponse> strategies() {
        return STRATEGIES.stream().map(GroupingStrategyResponse::from).toList();
    }

    @Transactional
    public GroupSetResponse generate(Long contextId, GenerateGroupsRequest request) {
        GroupingContext context = contextService.requireVisible(contextId);
        GroupingStrategy strategy = strategy(request.strategy());
        MatchingCriteria criteria = context.criteria();
        List<MatchingStudent> students = profileBuilder.build(profileService.profilesOf(context.getStudents()),
                criteria);
        long seed = request.seed() != null ? request.seed() : groupSetRepository.countByContextId(contextId) + 1;
        GroupFormation.FormationResult result = formation.form(students, context.getGroupSize(), criteria, strategy,
                seed);
        GroupSet set = GroupSet.of(context, currentUser.require(), strategy.code(), seed, result, LocalDateTime.now());
        return GroupSetResponse.from(groupSetRepository.save(set));
    }

    @Transactional(readOnly = true)
    public List<GroupSetSummaryResponse> history(Long contextId) {
        contextService.requireVisible(contextId);
        return groupSetRepository.findByContextIdOrderByGeneratedAtDescIdDesc(contextId).stream()
                .map(GroupSetSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupSetResponse findById(Long id) {
        return GroupSetResponse.from(requireVisible(id));
    }

    /** The generation, if the current user may see its project; otherwise reported as not existing. */
    @Transactional(readOnly = true)
    public GroupSet requireVisible(Long id) {
        GroupSet set = groupSetRepository.findById(id).orElse(null);
        if (set == null || !set.getContext().isVisibleTo(currentUser.require())) {
            throw new NotFoundException("Group set " + id + " does not exist");
        }
        return set;
    }

    private static GroupingStrategy strategy(String code) {
        String wanted = code == null || code.isBlank() ? BalancedGroupingStrategy.CODE : code.trim().toUpperCase();
        return STRATEGIES.stream()
                .filter(s -> s.code().equals(wanted))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Unknown strategy " + code + ". Use one of "
                        + STRATEGIES.stream().map(GroupingStrategy::code).toList()));
    }
}
