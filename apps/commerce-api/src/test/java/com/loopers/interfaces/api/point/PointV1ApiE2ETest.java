package com.loopers.interfaces.api.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointV1ApiE2ETest {

    private final TestRestTemplate testRestTemplate;

    private final PointJpaRepository pointJpaRepository;

    private final UserJpaRepository userJpaRepository;

    private final DatabaseCleanUp databaseCleanUp;

    private User user;

    @Autowired
    public PointV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            UserJpaRepository userJpaRepository,
            PointJpaRepository pointJpaRepository,
            DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userJpaRepository = userJpaRepository;
        this.pointJpaRepository = pointJpaRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @BeforeEach
    void setUp() {
        user = userJpaRepository.save(new User("test", "test@test.com", "2000-01-01", Gender.MALE));
        pointJpaRepository.save(new Point(user.getId()));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/points")
    @Nested
    class charge {

        private static final String ENDPOINT = "/api/v1/points";

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnTotalBalance_whenChargeIsSuccessful() {
            // arrange
            Long userId = user.getId();
            Long initialBalance = pointJpaRepository.findByUserId(userId).get().balance();
            Long chargeAmount = 1000L;
            PointV1Dto.ChargeRequest chargeRequest = new PointV1Dto.ChargeRequest(chargeAmount);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", userId.toString());
            HttpEntity<PointV1Dto.ChargeRequest> requestEntity = new HttpEntity<>(chargeRequest, headers);

            // act
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response = testRestTemplate.exchange(
                    ENDPOINT,
                    HttpMethod.POST,
                    requestEntity,
                    responseType
            );

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                    () -> assertThat(response.getBody().data()).isNotNull(),
                    () -> assertThat(response.getBody().data().amount()).isEqualTo(initialBalance + chargeAmount)
            );
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returnNotFound_whenUserDoesNotExist() {
            // arrange
            Long nonExistentUserId = 999L;
            PointV1Dto.ChargeRequest chargeRequest = new PointV1Dto.ChargeRequest(1000L);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", nonExistentUserId.toString());
            HttpEntity<PointV1Dto.ChargeRequest> requestEntity = new HttpEntity<>(chargeRequest, headers);

            // act
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response = testRestTemplate.exchange(
                    ENDPOINT,
                    HttpMethod.POST,
                    requestEntity,
                    responseType
            );

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }
    }

    @DisplayName("GET /api/v1/points")
    @Nested
    class retrieve {

        private static final String ENDPOINT = "/api/v1/points";

        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnPointBalance_whenRetrieveIsSuccessful() {
            //arrange
            Long balance = pointJpaRepository.findByUserId(user.getId()).get().balance();

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", user.getId().toString());
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            // act
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                    () -> assertThat(response.getBody().data()).isNotNull(),
                    () -> assertThat(response.getBody().data().amount()).isEqualTo(balance)
            );
        }

        @Test
        @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
        void returnsBadRequest_whenHeaderIsMissing() {
            // arrange
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            // act
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }
    }
}
