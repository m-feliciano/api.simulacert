package com.simulacert.attempt.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public enum Difficulty {
    EASY(1),
    MEDIUM(2),
    HARD(3);

    private static final Difficulty[] VALUES = values();

    @Getter
    private final int level;

    public List<Difficulty> getLessDifficultyThanThis() {
        return Arrays.stream(VALUES)
                .filter(d -> d.level < this.level)
                .toList();
    }
}

