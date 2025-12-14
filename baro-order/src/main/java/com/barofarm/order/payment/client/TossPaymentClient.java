package com.barofarm.order.payment.client;

import com.barofarm.order.common.exception.CustomException;
import com.barofarm.order.payment.application.dto.request.TossPaymentConfirmCommand;
import com.barofarm.order.payment.application.dto.request.TossPaymentRefundCommand;
import com.barofarm.order.payment.client.dto.TossPaymentResponse;
import com.barofarm.order.payment.exception.PaymentErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class TossPaymentClient {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String CANCEL_URL  = "https://api.tosspayments.com/v1/payments/%s/cancel";

    private final RestTemplate restTemplate;
    private final TossPaymentProperties properties;

    public TossPaymentClient(RestTemplate restTemplate, TossPaymentProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public TossPaymentResponse confirm(TossPaymentConfirmCommand command) {
        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new CustomException(PaymentErrorCode.INVALID_SECRET_KEY);
        }
        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", command.paymentKey());
        body.put("orderId", command.orderId());
        body.put("amount", command.amount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(CONFIRM_URL, entity, TossPaymentResponse.class);
        } catch (HttpStatusCodeException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            String responseBody = ex.getResponseBodyAsString();

            // TODO: log 남기기 (선택)
            log.error("Toss confirm failed. status={}, body={}", statusCode, responseBody, ex);

            int status = statusCode.value();
            if (status == 400) {
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_INVALID_REQUEST);
            } else if (status == 401) {
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_UNAUTHORIZED);
            } else if (status == 409) {
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_CONFLICT);
            } else if (status == 500 || status == 502 || status == 503) {
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_SERVER_ERROR);
            } else {
                // 그 외 애매한 코드들은 일단 공통 실패로 래핑
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_CONFIRM_FAILED);
            }
        }
    }

    public TossPaymentResponse refund(TossPaymentRefundCommand command) {
        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new CustomException(PaymentErrorCode.INVALID_SECRET_KEY);
        }

        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", command.cancelReason());

        String url = String.format(CANCEL_URL, command.paymentKey());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(url, entity, TossPaymentResponse.class);
        } catch (HttpStatusCodeException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            String responseBody = ex.getResponseBodyAsString();

            // TODO: log 남기기 (선택)
            // log.error("Toss cancel failed. status={}, body={}", statusCode, responseBody, ex);

            int status = statusCode.value();
            if (status == 400) {
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_INVALID_REQUEST);
            } else if (status == 401) {
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_UNAUTHORIZED);
            } else if (status == 409) {
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_CONFLICT);
            } else if (status == 500 || status == 502 || status == 503) {
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_SERVER_ERROR);
            } else {
                throw new CustomException(PaymentErrorCode.TOSS_PAYMENT_CANCEL_FAILED);
            }
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = properties.getSecretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return headers;
    }
}
