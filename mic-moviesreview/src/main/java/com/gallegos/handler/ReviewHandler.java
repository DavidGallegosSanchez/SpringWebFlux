package com.gallegos.handler;

import com.gallegos.domain.Review;
import com.gallegos.exception.ReviewDataException;
import com.gallegos.repository.ReviewReactiveRepository;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Validator;

import java.util.stream.Collectors;


@Component
@AllArgsConstructor
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private ReviewReactiveRepository reviewReactiveRepository;

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");
        if(movieInfoId.isPresent()) {
            var reviewsFlux = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewsResponse(reviewsFlux);
        } else {
            var reviewsFlux = reviewReactiveRepository.findAll();
            return buildReviewsResponse(reviewsFlux);
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                    .map(reqReview -> {
                        review.setComment(reqReview.getComment());
                        review.setRating(reqReview.getRating());
                        return review;
                    })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");

        return reviewReactiveRepository.findById(reviewId)
                .flatMap(review ->
                        reviewReactiveRepository.deleteById(reviewId)
                        .then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private static Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("constraintViolations: {}" , constraintViolations);
        if(!constraintViolations.isEmpty()) {
            var errorMessage = constraintViolations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new ReviewDataException(errorMessage);
        }
    }

}
