package com.gallegos.mic_moviesinfo.service;

import com.gallegos.mic_moviesinfo.domain.MovieInfo;
import com.gallegos.mic_moviesinfo.repository.MovieInfoRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class MoviesInfoService {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> getAllMoviesInfo() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> getAllMoviesInfoById(String id) {
        return movieInfoRepository.findById(id)
                .flatMap(Mono::just);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String id) {
        return movieInfoRepository.findById(id)
                .flatMap(movieInfo -> {
                    movieInfo.setCast(updatedMovieInfo.getCast());
                    movieInfo.setName(updatedMovieInfo.getName());
                    movieInfo.setYear(updatedMovieInfo.getYear());
                    movieInfo.setReleaseDate(updatedMovieInfo.getReleaseDate());
                    return movieInfoRepository.save(movieInfo);
                });
    }

    public Mono<Void> deleteMovieInfo(String id) {
        return movieInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> getMoviesInfoByYear(Integer year) {
        return movieInfoRepository.findByYear(year);
    }
}
