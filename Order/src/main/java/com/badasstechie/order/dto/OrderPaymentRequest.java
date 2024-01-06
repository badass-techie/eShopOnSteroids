package com.badasstechie.order.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * This is a record class that represents an order payment request to be sent to the payment microservice via the message bus and then to the payment gateway.
 *
 * @param orderId The order ID.
 * @param orderNumber The order number.
 * @param amount Total amount to be paid for in the smallest unit of the currency.
 * @param currency The currency in which the payment is made.
 * @param paymentMethod Exactly one of 'stripe', 'paypal', or 'mpesa'.
 * @param payerDetails A map of payer details. The keys are the names of the fields required by the payment gateway such as 'cardNumber', 'cvc', 'expiryMonth', 'expiryYear', and 'phoneNumber'.
 */
public record OrderPaymentRequest(
        Long orderId,
        String orderNumber,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        Map<String, String> payerDetails
) {}
