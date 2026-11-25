package pt.upt.studymatch.dto;

import pt.upt.studymatch.domain.GroupingStrategy;

public record GroupingStrategyResponse(String code, String description) {

    public static GroupingStrategyResponse from(GroupingStrategy strategy) {
        return new GroupingStrategyResponse(strategy.code(), strategy.description());
    }
}
