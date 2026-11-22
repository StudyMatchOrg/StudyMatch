package pt.upt.studymatch.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.GroupingContextRequest;
import pt.upt.studymatch.dto.GroupingContextResponse;
import pt.upt.studymatch.dto.GroupingContextSummaryResponse;
import pt.upt.studymatch.dto.StudentResponse;
import pt.upt.studymatch.service.GroupingContextService;

@RestController
@RequestMapping("/api/grouping-contexts")
public class GroupingContextController {

    private final GroupingContextService contextService;

    public GroupingContextController(GroupingContextService contextService) {
        this.contextService = contextService;
    }

    @GetMapping
    public List<GroupingContextSummaryResponse> findAll() {
        return contextService.findAll();
    }

    @GetMapping("/{id}")
    public GroupingContextResponse findById(@PathVariable Long id) {
        return contextService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupingContextResponse create(@Valid @RequestBody GroupingContextRequest request) {
        return contextService.create(request);
    }

    @PutMapping("/{id}")
    public GroupingContextResponse update(@PathVariable Long id, @Valid @RequestBody GroupingContextRequest request) {
        return contextService.update(id, request);
    }

    @GetMapping("/candidates")
    public List<StudentResponse> candidates(@RequestParam Long courseUnitId, @RequestParam String academicYear) {
        return contextService.candidates(courseUnitId, academicYear);
    }
}
