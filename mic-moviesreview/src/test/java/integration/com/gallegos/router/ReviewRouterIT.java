package integration.com.gallegos.router;

import com.gallegos.MoviesReviewServiceApplication;
import com.gallegos.domain.Review;
import com.gallegos.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
classes = MoviesReviewServiceApplication.class)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewRouterIT {

    static String REVIEWS_URL = "/v1/reviews";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewReactiveRepository reviewReactiveRepository;

    @BeforeEach
    void setup() {
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when & then
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var savedReview = reviewResponse.getResponseBody();
                    assert savedReview != null;
                    assertNotNull(savedReview.getReviewId());
                });
    }

    @Test
    void getReviews() {
        //given

        //when & then
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
    void getReviewsById() {
        //given
        var movieInfoId = 1L;

        //when & then
        webTestClient
                .get()
                .uri(REVIEWS_URL + "?movieInfoId=" + movieInfoId )
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void updateReview() {
        // given
        var existingReview = reviewReactiveRepository.findAll().blockFirst();
        assertNotNull(existingReview);

        var reviewId = existingReview.getReviewId();

        var updateReview = new Review(
                reviewId,
                existingReview.getMovieInfoId(),
                "Awesome Movie updated",
                9.0
        );

        //when & then
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", reviewId)
                .bodyValue(updateReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var updatedReview = reviewEntityExchangeResult.getResponseBody();
                    assert updatedReview != null;
                    assertEquals("Awesome Movie updated", updatedReview.getComment());
                });
    }

    @Test
    void deleteReview() {
        // given
        var existingReview = reviewReactiveRepository.findAll().blockFirst();
        assertNotNull(existingReview);

        var reviewId = existingReview.getReviewId();

        //when & then
        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
