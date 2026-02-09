package com.gallegos.reactive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

@SpringBootTest
class ReactiveApplicationTests {

	@Autowired
	ReactiveApplication reactiveApplication;

	@Test
	void namesFlux() {
		// given

		// when
		var fluxNames = reactiveApplication.namesFlux();

		// then
		StepVerifier.create(fluxNames)
				.expectNext("Leonardo", "Donatello", "Michelangelo", "Raphael")
				.verifyComplete();

		StepVerifier.create(fluxNames)
				.expectNext("Leonardo")
				.expectNextCount(3)
				.verifyComplete();
	}

	@Test
	void namesMono() {
		// given

		// when
		var monoName = reactiveApplication.nameMono();

		// then
		StepVerifier.create(monoName)
				.expectNext("Splinter")
				.verifyComplete();
		StepVerifier.create(monoName)
				.expectNextCount(1)
				.verifyComplete();
	}

	@Test
	void namesFluxMap() {
		// given

		// when
		var namesFluxMap = reactiveApplication.namesFluxMap();

		// then
		StepVerifier.create(namesFluxMap)
				.expectNext("LEONARDO", "DONATELLO", "MICHELANGELO", "RAPHAEL")
				.verifyComplete();

		StepVerifier.create(namesFluxMap)
				.expectNext("LEONARDO")
				.expectNextCount(3)
				.verifyComplete();
	}

	@Test
	void namesFluxMapImmutable() {
		// given

		// when
		var  namesFluxMapImmutable = reactiveApplication.namesFluxMapImmutable();

		// then
		StepVerifier.create(namesFluxMapImmutable)
				.expectNext("Leonardo", "Donatello", "Michelangelo", "Raphael")
				.verifyComplete();
	}

	@Test
	void namesFluxFilter() {
		// given
		var length = 8;

		// when
		var namesFilter = reactiveApplication.namesFluxFilter(length);

		// then
		StepVerifier.create(namesFilter)
				.expectNext("DONATELLO",  "MICHELANGELO")
				.verifyComplete();
	}

	@Test
	void namesFluxFlatMap() {
		// given
		var length = 11;

		// when
		var namesFlatMap = reactiveApplication.namesFluxFlatMap(length);

		// then
		StepVerifier.create(namesFlatMap)
				.expectNext("M","I", "C", "H", "E", "L", "A", "N", "G", "E", "L", "O")
				.verifyComplete();
	}

	@Test
	void namesFluxFlatMapAsync() {
		// given
		var length = 11;

		// when
		var namesFlatMap = reactiveApplication.namesFluxFlatMapAsync(length);

		// then
		StepVerifier.create(namesFlatMap)
				//.expectNext("M","I", "C", "H", "E", "L", "A", "N", "G", "E", "L", "O")
				.expectNextCount(12)
				.verifyComplete();
	}

	@Test
	void namesFluxConcatMap() {
		// given
		var length = 11;

		// when
		var namesFlatMap = reactiveApplication.namesFluxConcatMap(length);

		// then
		StepVerifier.create(namesFlatMap)
				.expectNext("M","I", "C", "H", "E", "L", "A", "N", "G", "E", "L", "O")
				//.expectNextCount(12)
				.verifyComplete();
	}

	@Test
	void namesMonoFlatMap() {
		// given

		// when
		var namesMonoFaltMap = reactiveApplication.namesMonoFlatMap();

		// then
		StepVerifier.create(namesMonoFaltMap)
				.expectNext(List.of("L","E","O","N","A","R","D","O"))
				.verifyComplete();
	}

	@Test
	void namesMonoFlatMapMany() {
		// given

		// when
		var namesMonoFaltMap = reactiveApplication.namesMonoFlatMapMany();

		// then
		StepVerifier.create(namesMonoFaltMap)
				.expectNext("L","E","O","N","A","R","D","O")
				.verifyComplete();
	}

	@Test
	void exploreConcat() {
		// given

		// when
		var concat = reactiveApplication.exploreConcat();

		// then
		StepVerifier.create(concat)
				.expectNext("A", "B", "C", "D", "E", "F")
				.verifyComplete();
	}

	@Test
	void exploreConcatWith() {
		// given

		// when
		var concat = reactiveApplication.exploreConcatWith();

		// then
		StepVerifier.create(concat)
				.expectNext("A", "B", "C", "D", "E", "F")
				.verifyComplete();
	}

	@Test
	void exploreMerge() {
		// given

		// when
		var concat = reactiveApplication.exploreMerge();

		// then
		StepVerifier.create(concat)
				.expectNext("A", "D", "B", "E", "C", "F")
				.verifyComplete();
	}

	@Test
	void exploreMergeWith() {
		// given

		// when
		var concat = reactiveApplication.exploreMergeWith();

		// then
		StepVerifier.create(concat)
				.expectNext("A", "D", "B", "E", "C", "F")
				.verifyComplete();
	}

}
