package com.gallegos.client;

import com.gallegos.domain.Review;
import com.gallegos.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class ReviewRestClient {

    private WebClient webClient;

    @Value("${restClient.reviewUrl}")
    private String reviewsUrl;

    public ReviewRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId) {
        var url = UriComponentsBuilder.fromUriString(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .toUriString();

        return webClient.get()
                .uri(url)
                .exchangeToFlux(response -> {

                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Flux.empty();
                    }

                    if (response.statusCode().is5xxServerError()) {
                        return response.bodyToMono(String.class)
                                .flatMapMany(msg ->
                                        Flux.error(new ReviewsServerException(msg)));
                    }

                    return response.bodyToFlux(Review.class);
                });
    }
}
