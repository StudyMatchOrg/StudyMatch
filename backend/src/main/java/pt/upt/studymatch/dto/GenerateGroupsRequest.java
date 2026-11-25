package pt.upt.studymatch.dto;

/**
 * @param strategy the strategy code, e.g. {@code BALANCED} (the default) or {@code RANDOM}
 * @param seed     optional; the same seed and data give the same groups. Without one, each
 *                 generation of a project uses the next number.
 */
public record GenerateGroupsRequest(String strategy, Long seed) {
}
