package com.simulacert.attempt.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public enum Difficulty {
    ANY(0),
    EASY(1),
    MEDIUM(2),
    HARD(3);

    private static final Difficulty[] VALUES = values();

    @Getter
    private final int level;

    public static Difficulty[] all() {
        return Arrays.stream(VALUES)
                .filter(d -> d != ANY)
                .toArray(Difficulty[]::new);
    }

    public List<Difficulty> getLessDifficultyThanThis() {
        return Arrays.stream(all())
                .filter(d -> d.level < this.level)
                .toList();
    }
}

