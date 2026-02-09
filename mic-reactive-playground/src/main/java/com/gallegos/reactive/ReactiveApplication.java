package com.gallegos.reactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;

@SpringBootApplication
@Slf4j
public class ReactiveApplication {

	Flux<String> namesFlux() {
		return Flux.fromIterable(List.of("Leonardo", "Donatello", "Michelangelo", "Raphael"))
				.log();
	}

	Mono<String> nameMono() {
		return Mono.just("Splinter")
				.log();
	}

	Flux<String> namesFluxMap() {
		return Flux.fromIterable(List.of("Leonardo", "Donatello", "Michelangelo", "Raphael"))
				.map(String::toUpperCase)
				.log();
	}

	Flux<String> namesFluxMapImmutable() {
		var names = Flux.fromIterable(List.of("Leonardo", "Donatello", "Michelangelo", "Raphael"));
		names.map(String::toUpperCase);
		return names;
	}

	Flux<String> namesFluxFilter(final int length) {
		return Flux.fromIterable(List.of("Leonardo", "Donatello", "Michelangelo", "Raphael"))
				.map(String::toUpperCase)
				.filter(name -> name.length() > length)
				.log();
	}

	Flux<String> namesFluxFlatMap(final int length) {
		return Flux.fromIterable(List.of("Leonardo", "Donatello", "Michelangelo", "Raphael"))
				.map(String::toUpperCase)
				.filter(name -> name.length() > length)
				.flatMap(this::splitString)
				.log();
	}

	Flux<String> namesFluxFlatMapAsync(final int length) {
		return Flux.fromIterable(List.of("Leonardo", "Donatello", "Michelangelo", "Raphael"))
				.map(String::toUpperCase)
				.filter(name -> name.length() > length)
				.flatMap(this::splitStringWithDelay)
				.log();
	}

	Flux<String> namesFluxConcatMap(final int length) {
		return Flux.fromIterable(List.of("Leonardo", "Donatello","Michelangelo", "Raphael"))
				.map(String::toUpperCase)
				.filter(name -> name.length() > length)
				.concatMap(this::splitStringWithDelay);
	}

	private Flux<String> splitStringWithDelay(String str) {
		var charArray = str.split("");
		var delay = new Random().nextInt(1000);
		return Flux.fromArray(charArray)
				.delayElements(Duration.ofMillis(delay));
	}

	Mono<List<String>> namesMonoFlatMap() {
		return Mono.just("Leonardo")
				.map(String::toUpperCase)
				.flatMap(this::splitStringMono); // Mono<List of L, E, O, N, A, R, D, O>
	}

	private Mono<List<String>> splitStringMono(String str) {
		var charArray = str.split("");
		var charList = List.of(charArray);
		return Mono.just(charList);
	}

	Flux<String> namesMonoFlatMapMany() {
		return Mono.just("Leonardo")
				.map(String::toUpperCase)
				.flatMapMany(this::splitString);
	}

	private Flux<String> splitString(String str) {
		var charArray = str.split("");
		return Flux.fromArray(charArray);
	}

	Flux<String> exploreConcat() {
		var abcFlux = Flux.just("A", "B", "C");
		var defFlux = Flux.just("D", "E", "F");

		return Flux.concat(abcFlux, defFlux);
	}

	Flux<String> exploreConcatWith() {
		var abcFlux = Flux.just("A", "B", "C");
		var defFlux = Flux.just("D", "E", "F");

		return abcFlux.concatWith(defFlux);
	}

	Flux<String> exploreMerge() {
		var abcFlux = Flux.just("A", "B", "C")
				.delayElements(Duration.ofMillis(100));
		var defFlux = Flux.just("D", "E", "F")
				.delayElements(Duration.ofMillis(125));

		return Flux.merge(abcFlux, defFlux).log();
	}

	Flux<String> exploreMergeWith() {
		var abcFlux = Flux.just("A", "B", "C")
				.delayElements(Duration.ofMillis(100));
		var defFlux = Flux.just("D", "E", "F")
				.delayElements(Duration.ofMillis(125));

		return abcFlux.mergeWith(defFlux).log();
	}

	public static void main(String[] args) {

		ReactiveApplication reactiveApplication = new ReactiveApplication();

		reactiveApplication.namesFlux()
				.subscribe(name -> log.info("The name is: {}", name));

		reactiveApplication.nameMono()
				.subscribe(name -> log.info("The name is: {}", name));

		reactiveApplication.namesFluxMap()
				.subscribe(name -> log.info("The name in Upper Case is: {}", name));

		reactiveApplication.namesFluxMapImmutable()
				.subscribe(name -> log.info("The name in Upper Case Immutable is: {}", name));

		reactiveApplication.namesFluxFilter(8)
				.subscribe(name -> log.info("The name with length bigger than 8 is {}", name));

	}

}
