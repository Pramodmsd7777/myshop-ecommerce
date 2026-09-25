package com.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentDtos {
    public record PaymentCreateRequest(@NotNull Long orderId) {}

    public record PaymentVerifyRequest(
        @NotBlank String razorpayOrderId,
        @NotBlank String razorpayPaymentId,
        @NotBlank String razorpaySignature) {}

    public record PaymentInitResponse(String keyId, String razorpayOrderId, long amount,
                                      String currency, Long orderId) {}
}
