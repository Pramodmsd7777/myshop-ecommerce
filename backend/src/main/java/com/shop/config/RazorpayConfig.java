package com.shop.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {
    @Bean
    public RazorpayClient razorpayClient(@Value("${razorpay.key-id}") String keyId,
                                         @Value("${razorpay.key-secret}") String keySecret)
            throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}
