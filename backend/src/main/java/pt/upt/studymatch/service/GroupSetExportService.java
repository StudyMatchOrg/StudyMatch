package pt.upt.studymatch.service;

import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.GroupMember;
import pt.upt.studymatch.domain.GroupSet;
import pt.upt.studymatch.domain.StudentGroup;

/**
 * Exports a generation of groups as CSV (UC7), e.g. to publish them on Moodle. Uses ';' as the
 * separator and a UTF-8 byte order mark, so the file opens directly in Excel with Portuguese
 * settings and accents.
 */
@Service
public class GroupSetExportService {

    static final String HEADER = "Project;Generated at;Group;Student number;Name;Email;Strongest competency";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GroupGenerationService generationService;

    public GroupSetExportService(GroupGenerationService generationService) {
        this.generationService = generationService;
    }

    @Transactional(readOnly = true)
    public Export export(Long groupSetId) {
        GroupSet set = generationService.requireVisible(groupSetId);
        String generatedAt = set.getGeneratedAt().format(DATE_TIME);
        Stream<String> rows = set.getGroups().stream()
                .flatMap(group -> group.getMembers().stream().map(member -> row(set, generatedAt, group, member)));
        String csv = "﻿" + Stream.concat(Stream.of(HEADER), rows).collect(Collectors.joining("\r\n")) + "\r\n";
        return new Export(fileName(set), csv);
    }

    private static String row(GroupSet set, String generatedAt, StudentGroup group, GroupMember member) {
        return List.of(set.getContext().getName(), generatedAt, String.valueOf(group.getNumber()),
                        member.getStudent().getStudentNumber(), member.getStudent().getName(),
                        member.getStudent().getEmail(),
                        member.getStrongestCompetency() == null ? "" : member.getStrongestCompetency())
                .stream()
                .map(GroupSetExportService::cell)
                .collect(Collectors.joining(";"));
    }

    /**
     * One CSV cell. Values with separators, quotes or line breaks are quoted. Values that a
     * spreadsheet would run as a formula (starting with =, +, - or @) get a leading apostrophe, so
     * a malicious student name cannot run code when the file is opened (CSV injection).
     */
    static String cell(String value) {
        String safe = value;
        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        if (safe.contains(";") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            safe = "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    /** e.g. {@code groups-aed-2026-27-project-groups-12.csv}: only letters, digits and dashes. */
    private static String fileName(GroupSet set) {
        String project = Normalizer.normalize(set.getContext().getName(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return "groups-" + project + "-" + set.getId() + ".csv";
    }

    public record Export(String fileName, String content) {
    }
}
