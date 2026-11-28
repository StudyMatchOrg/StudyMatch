package pt.upt.studymatch.controller;

import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.service.GroupSetExportService;

@RestController
public class GroupSetExportController {

    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final GroupSetExportService exportService;

    public GroupSetExportController(GroupSetExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/api/group-sets/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        GroupSetExportService.Export export = exportService.export(id);
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(export.fileName()).build().toString())
                .body(export.content().getBytes(StandardCharsets.UTF_8));
    }
}
