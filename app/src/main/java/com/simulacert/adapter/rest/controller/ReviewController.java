package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.ReviewControllerOpenApi;
import com.simulacert.auth.domain.User;
import com.simulacert.review.application.dto.CreateReviewRequest;
import com.simulacert.review.application.dto.ReviewResponse;
import com.simulacert.review.application.port.in.CreateReviewUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        log.info("Creating review for attempt {} by user {}", request.attemptId(), user.getId());

        ReviewResponse response = createReviewUseCase.createReview(user.getId(), request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Override
    @GetMapping("/by-attempt/{attemptId}")
    public ResponseEntity<ReviewResponse> getReviewByAttempt(@PathVariable UUID attemptId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        log.debug("Getting review for attempt {} by user {}", attemptId, user.getId());

        return createReviewUseCase.getReviewByAttempt(user.getId(), attemptId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

