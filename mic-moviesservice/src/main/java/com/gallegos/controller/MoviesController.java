package com.gallegos.controller;

import com.gallegos.client.MoviesInfoRestClient;
import com.gallegos.client.ReviewRestClient;
import com.gallegos.domain.Movie;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
@AllArgsConstructor
public class MoviesController {

    private MoviesInfoRestClient moviesInfoRestClient;
    private ReviewRestClient reviewRestClient;

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId) {
        return moviesInfoRestClient.retrieveMovieInfoById(movieId)
                .flatMap(moviesInfo -> {
                    var reviewsListMono = reviewRestClient.retrieveReviews(movieId)
                            .collectList();
                    return reviewsListMono.map(reviews -> new Movie(moviesInfo, reviews));
                });
    }
}
