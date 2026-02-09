package com.gallegos.mic_moviesinfo.integration.repository;

import com.gallegos.mic_moviesinfo.domain.MovieInfo;
import com.gallegos.mic_moviesinfo.repository.MovieInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIT {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setup() {
        var movieinfos = List.of(
                new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );

        movieInfoRepository.saveAll(movieinfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {
        // given

        // when
        var movieInfosFlux = movieInfoRepository.findAll();

        // then
        StepVerifier.create(movieInfosFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        // given

        // when
        var movieInfoMono = movieInfoRepository.findById("abc");

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        // given
        var movie = new MovieInfo(null, "SpiderMan",
                2000, List.of("Peter Parker"), LocalDate.parse("2000-08-10"));
        // when
        var movieInfoMono = movieInfoRepository.save(movie).log();

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("SpiderMan", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        // given
        var movieInfo = movieInfoRepository.findById("abc").block();
        assertNotNull(movieInfo);
        movieInfo.setYear(2021);

        // when
        var movieInfoMono = movieInfoRepository.save(movieInfo).log();

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movie -> {
                    assertEquals(2021, movie.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        // given

        // when
        movieInfoRepository.deleteById("abc").block();
        var moviesInfo = movieInfoRepository.findAll().log();

        // then
        StepVerifier.create(moviesInfo)
                .expectNextMatches(movie -> !movie.getMovieInfoId().equals("abc"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByYear(){
        // given

        // when
        var moviesByYear = movieInfoRepository.findByYear(2005);

        // then
        StepVerifier.create(moviesByYear)
                .expectNextMatches(movie -> movie.getYear().equals(2005))
                .verifyComplete();
    }

    @Test
    void findByName(){
        // given

        // when
        var moviesByYear = movieInfoRepository.findByName("Batman Begins");

        // then
        StepVerifier.create(moviesByYear)
                .assertNext(movie -> movie.getName().equals("Batman Begins"))
                .verifyComplete();
    }

}
