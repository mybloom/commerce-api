package com.loopers.application.payment;

import com.loopers.support.error.CardPaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalCardServiceImpl implements ExternalCardService {

    @Override
    public String requestPayment(String cardNumber, String cardType, Long orderId) throws CardPaymentException {
        log.info("PG사 카드 결제 요청 - orderId: {}, cardType: {}, cardNumber: {}****",
                orderId, cardType, maskCardNumber(cardNumber));

        try {
            // 1. 카드 번호 기본 검증
            validateCardNumber(cardNumber);

            // 2. 카드 타입 검증
            validateCardType(cardType);

            // 3. PG사 API 호출 시뮬레이션
            String externalPaymentId = callPgApi(cardNumber, cardType, orderId);

            log.info("PG사 카드 결제 요청 성공 - orderId: {}, externalPaymentId: {}",
                    orderId, externalPaymentId);

            return externalPaymentId;

        } catch (Exception e) {
            log.error("PG사 카드 결제 요청 실패 - orderId: {}, error: {}", orderId, e.getMessage());
            throw new CardPaymentException("PG사 결제 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * PG사 API 호출 시뮬레이션
     */
    private String callPgApi(String cardNumber, String cardType, Long orderId) {
        // 실제로는 HTTP 클라이언트로 PG사 API 호출
        // 여기서는 시뮬레이션

        // 특정 카드번호로 실패 시뮬레이션
        if (cardNumber.startsWith("0000")) {
            throw new RuntimeException("유효하지 않은 카드입니다");
        }

        if (cardNumber.startsWith("9999")) {
            throw new RuntimeException("결제 한도를 초과했습니다");
        }

        // 성공 시 외부 결제 ID 생성
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);

        return String.format("PG_%s_%s_%s", cardType, timestamp, randomId);
    }

    /**
     * 카드 번호 기본 검증
     */
    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("카드 번호가 없습니다");
        }

        // 하이픈 제거 후 숫자만 추출
        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");

        if (cleanNumber.length() < 13 || cleanNumber.length() > 19) {
            throw new IllegalArgumentException("카드 번호 길이가 올바르지 않습니다");
        }

        // 루나 알고리즘 검증 (간단 버전)
        if (!isValidLuhnNumber(cleanNumber)) {
            throw new IllegalArgumentException("유효하지 않은 카드 번호입니다");
        }
    }

    /**
     * 카드 타입 검증
     */
    private void validateCardType(String cardType) {
        if (cardType == null || cardType.trim().isEmpty()) {
            throw new IllegalArgumentException("카드 타입이 없습니다");
        }

        // 지원하는 카드 타입 검증
        String[] supportedTypes = {"VISA", "MASTER", "AMEX", "JCB", "DINERS"};
        boolean isSupported = false;
        for (String type : supportedTypes) {
            if (type.equalsIgnoreCase(cardType)) {
                isSupported = true;
                break;
            }
        }

        if (!isSupported) {
            throw new IllegalArgumentException("지원하지 않는 카드 타입입니다: " + cardType);
        }
    }

    /**
     * 루나 알고리즘 검증 (카드 번호 유효성)
     */
    private boolean isValidLuhnNumber(String cardNumber) {
        int sum = 0;
        boolean isEven = false;

        // 뒤에서부터 검사
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (isEven) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit / 10 + digit % 10;
                }
            }

            sum += digit;
            isEven = !isEven;
        }

        return sum % 10 == 0;
    }

    /**
     * 카드 번호 마스킹 (로그용)
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        if (cleanNumber.length() < 4) {
            return "****";
        }

        return cleanNumber.substring(0, 4) + "****" + cleanNumber.substring(cleanNumber.length() - 4);
    }
}
