package com.gallegos.client;

import com.gallegos.domain.MovieInfo;
import com.gallegos.exception.MoviesInfoClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfoById(String movieId) {
        var url = moviesInfoUrl.concat("/{id}");
        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException(
                                "No MovieInfo found for id: " + movieId,
                                clientResponse.statusCode().value()
                        ));
                    }

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorMessage ->
                                    Mono.error(new MoviesInfoClientException(
                                            errorMessage,
                                            clientResponse.statusCode().value()
                                    )));
                })
                .bodyToMono(MovieInfo.class)
                .log();
    }
}
