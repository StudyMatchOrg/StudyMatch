package pt.upt.studymatch.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.GenerateGroupsRequest;
import pt.upt.studymatch.dto.GroupSetResponse;
import pt.upt.studymatch.dto.GroupSetSummaryResponse;
import pt.upt.studymatch.dto.GroupingStrategyResponse;
import pt.upt.studymatch.service.GroupGenerationService;

@RestController
@RequestMapping("/api")
public class GroupSetController {

    private final GroupGenerationService generationService;

    public GroupSetController(GroupGenerationService generationService) {
        this.generationService = generationService;
    }

    @GetMapping("/grouping-strategies")
    public List<GroupingStrategyResponse> strategies() {
        return generationService.strategies();
    }

    @PostMapping("/grouping-contexts/{contextId}/group-sets")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupSetResponse generate(@PathVariable Long contextId,
            @RequestBody(required = false) GenerateGroupsRequest request) {
        return generationService.generate(contextId, request == null ? new GenerateGroupsRequest(null, null) : request);
    }

    @GetMapping("/grouping-contexts/{contextId}/group-sets")
    public List<GroupSetSummaryResponse> history(@PathVariable Long contextId) {
        return generationService.history(contextId);
    }

    @GetMapping("/group-sets/{id}")
    public GroupSetResponse findById(@PathVariable Long id) {
        return generationService.findById(id);
    }
}
