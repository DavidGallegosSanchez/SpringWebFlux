package unit;

import com.gallegos.domain.Review;
import com.gallegos.exceptionhandler.GlobalErrorHandler;
import com.gallegos.handler.ReviewHandler;
import com.gallegos.repository.ReviewReactiveRepository;
import com.gallegos.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {
        ReviewRouter.class,
        ReviewHandler.class,
        GlobalErrorHandler.class
})
@AutoConfigureWebTestClient
class ReviewsTest {

    private static String REVIEWS_URL = "/v1/reviews";

    @MockitoBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void addReview() {
        // given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        // when
        when(reviewReactiveRepository.save(review))
                .thenReturn(Mono.just(new Review("abs", 1L, "Rush", 9.0)));

        // then
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                    assertEquals("Rush", savedReview.getComment());
                });
    }

    @Test
    void getReviews() {
        // given
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        // when
        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviewsList));

        // then
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReview() {
        // given
        var updatedReview =  new Review("mockId", 1L, "Awesome Movie updated", 9.0);
        var existingReview = new Review("id", 1L, "Awesome Movie", 9.0);

        // when
        when(reviewReactiveRepository.findById("id")).thenReturn(Mono.just(existingReview));
        when(reviewReactiveRepository.save(any(Review.class))).thenReturn(Mono.just(updatedReview));

        // then
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", "id")
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var updated = reviewEntityExchangeResult.getResponseBody();
                    assert updated != null;
                    assertEquals("Awesome Movie updated", updated.getComment());
                });
    }

    @Test
    void deleteReview() {
        // given
        var existingReview = new Review("id", 1L, "Awesome Movie", 9.0);

        // when
        when(reviewReactiveRepository.findById("id")).thenReturn(Mono.just(existingReview));
        when(reviewReactiveRepository.deleteById(existingReview.getReviewId())).thenReturn(Mono.empty());

        // then
        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", "id")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void addReview_validation() {
        // given
        var review = new Review(null, null, "Awesome Movie", -9.0);

        // when
        when(reviewReactiveRepository.save(review))
                .thenReturn(Mono.just(new Review("abs", 1L, "Rush", 9.0)));

        // then
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    assertThat(responseBody)
                            .isNotNull()
                            .contains("rating.movieInfoId : must not be null")
                            .contains("rating.negative : please pass a non-negative value");
                });

    }

}
