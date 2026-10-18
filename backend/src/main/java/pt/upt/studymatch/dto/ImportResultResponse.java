package pt.upt.studymatch.dto;

import java.util.List;

/**
 * Outcome of an import: how many lines were saved, and every rejected line with the reason.
 */
public record ImportResultResponse(int totalLines, int imported, List<RejectedLine> rejected) {

    public record RejectedLine(int line, String message) {
    }
}
