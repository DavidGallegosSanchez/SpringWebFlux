package com.gallegos.mic_moviesinfo.unit.controller;

import com.gallegos.mic_moviesinfo.controller.MoviesInfoController;
import com.gallegos.mic_moviesinfo.domain.MovieInfo;
import com.gallegos.mic_moviesinfo.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerTest {

    private static String MOVIES_INFO_URL = "/v1/movieinfos";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private MoviesInfoService moviesInfoService;

    @Test
    void getAllMoviesInfo() {
        // given
        var movieinfos = List.of(
                new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );

        // when
        when(moviesInfoService.getAllMoviesInfo()).thenReturn(Flux.fromIterable(movieinfos));

        // then
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfosById() {
        // given
        var movieInfoId = "abc";
        var movieInfoMono = Mono.just(new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        // when
        when(moviesInfoService.getAllMoviesInfoById(movieInfoId)).thenReturn(movieInfoMono);

        // then
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises")
                .jsonPath("$.year").isEqualTo(2012);
    }

    @Test
    void addMovieInfo() {
        // given
        var movieInfo = new MovieInfo(
                "mockId",
                "Batman Begins",
                2005,
                List.of("Christian Bale", "Michael Cane"),
                LocalDate.parse("2005-06-15")
        );

        // when
        when(moviesInfoService.addMovieInfo(movieInfo)).thenReturn(Mono.just(movieInfo));

        // then
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void updateMovieInfo() {
        // given
        var updatedMovieInfo = new MovieInfo("mockId", "Dark Knight Rises updated",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")
        );
        var movieInfoId = "mockId";

        // when
        when(moviesInfoService.updateMovieInfo(updatedMovieInfo, movieInfoId)).thenReturn(Mono.just(updatedMovieInfo));

        // then
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updated = movieInfoEntityExchangeResult.getResponseBody();
                    assert updated != null;
                    assertEquals("Dark Knight Rises updated", updated.getName());
                });
    }

    @Test
    void deleteMovieInfo() {
        // given
        var movieInfoId = "abc";

        // when
        when(moviesInfoService.deleteMovieInfo(movieInfoId)).thenReturn(Mono.empty());

        // then
        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void addMovieInfo_validation() {
        // given empty name, cast empty list and negative year
        var movieInfo = new MovieInfo(
                "mockId",
                "",
                -2005,
                List.of(""),
                LocalDate.parse("2005-06-15")
        );

        // when

        // then
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    assertThat(responseBody)
                            .isNotNull()
                            .contains("movieInfo.name must be present")
                            .contains("movieInfo.year must be a positive value")
                            .contains("movieInfo.cast must be present");
                });
    }

    @Test
    void updateMovieInfo_notFound() {
        // given
        var updatedMovieInfo = new MovieInfo("mockId", "Dark Knight Rises updated",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")
        );
        var movieInfoId = "mockId";

        // when
        when(moviesInfoService.updateMovieInfo(updatedMovieInfo, movieInfoId)).thenReturn(Mono.empty());

        // then
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getMovieInfosByI_notFound() {
        // given
        var movieInfoId = "abc";

        // when
        when(moviesInfoService.getAllMoviesInfoById(movieInfoId)).thenReturn(Mono.empty());

        // then
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
