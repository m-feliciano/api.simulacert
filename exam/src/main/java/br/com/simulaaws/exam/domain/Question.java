package br.com.simulaaws.exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID examId;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false, length = 100)
    private String domain;

    @Column(nullable = false, length = 50)
    private String difficulty;
}
