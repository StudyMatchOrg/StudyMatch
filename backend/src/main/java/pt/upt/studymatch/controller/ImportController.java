package pt.upt.studymatch.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pt.upt.studymatch.dto.ImportResultResponse;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.service.AcademicRecordImportService;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final AcademicRecordImportService importService;

    public ImportController(AcademicRecordImportService importService) {
        this.importService = importService;
    }

    @PostMapping(value = "/academic-records", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResultResponse importAcademicRecords(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessRuleException("The file is empty");
        }
        return importService.importCsv(new String(file.getBytes(), StandardCharsets.UTF_8));
    }
}
