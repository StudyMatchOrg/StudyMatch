package pt.upt.studymatch.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.dto.ImportResultResponse;
import pt.upt.studymatch.dto.ImportResultResponse.RejectedLine;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.exception.ConflictException;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.StudentRepository;

/**
 * Imports academic records from a CSV file (team-defined feature, use case UC15).
 *
 * <p>Each line is saved in its own transaction with the same rules as the form
 * ({@link AcademicRecordService#record}), so invalid lines are rejected with a reason while the
 * valid lines are still imported.
 */
@Service
public class AcademicRecordImportService {

    private final AcademicRecordCsvParser parser = new AcademicRecordCsvParser();
    private final AcademicRecordService recordService;
    private final StudentRepository studentRepository;
    private final CourseUnitRepository courseUnitRepository;
    private final TransactionTemplate transaction;

    public AcademicRecordImportService(AcademicRecordService recordService, StudentRepository studentRepository,
            CourseUnitRepository courseUnitRepository, PlatformTransactionManager transactionManager) {
        this.recordService = recordService;
        this.studentRepository = studentRepository;
        this.courseUnitRepository = courseUnitRepository;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    public ImportResultResponse importCsv(String content) {
        AcademicRecordCsvParser.Result parsed = parser.parse(content);
        List<RejectedLine> rejected = new ArrayList<>();
        parsed.errors().forEach(error -> rejected.add(new RejectedLine(error.line(), error.message())));

        int imported = 0;
        for (AcademicRecordCsvParser.Row row : parsed.rows()) {
            try {
                transaction.executeWithoutResult(status -> importRow(row));
                imported++;
            } catch (BusinessRuleException | ConflictException ex) {
                rejected.add(new RejectedLine(row.line(), ex.getMessage()));
            }
        }
        rejected.sort(Comparator.comparingInt(RejectedLine::line));
        return new ImportResultResponse(parsed.rows().size() + parsed.errors().size(), imported, rejected);
    }

    private void importRow(AcademicRecordCsvParser.Row row) {
        Student student = studentRepository.findByStudentNumber(row.studentNumber())
                .orElseThrow(() -> new BusinessRuleException("Unknown student number " + row.studentNumber()));
        CourseUnit courseUnit = courseUnitRepository.findByCode(row.courseUnitCode())
                .orElseThrow(() -> new BusinessRuleException("Unknown course unit " + row.courseUnitCode()));
        recordService.record(student, courseUnit, row.academicYear(), row.semester(), row.status(), row.grade());
    }
}
