package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.ReviewControllerOpenApi;
import com.simulacert.review.application.dto.CreateReviewRequest;
import com.simulacert.review.application.dto.ReviewResponse;
import com.simulacert.review.application.dto.ReviewSummaryResponse;
import com.simulacert.review.application.port.in.CreateReviewUseCase;
import com.simulacert.util.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewControllerOpenApi {

    private final CreateReviewUseCase createReviewUseCase;

    @Override
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = createReviewUseCase.createReview(UserContextHolder.getUser(), request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @Override
    @GetMapping("/by-attempt/{attemptId}")
    public ResponseEntity<ReviewResponse> getReviewByAttempt(@PathVariable UUID attemptId) {
        return createReviewUseCase.getReviewByAttempt(UserContextHolder.getUser(), attemptId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Override
    @GetMapping("/summary/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<ReviewSummaryResponse> getSummaryByUser(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(createReviewUseCase.getSummaryByUser(userId));
    }
}

