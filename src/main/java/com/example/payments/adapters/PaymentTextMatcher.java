package com.example.payments.adapters;

final class PaymentTextMatcher {
    private PaymentTextMatcher() {
    }

    static boolean contains(String source, String fragment) {
        return source != null && source.toLowerCase().contains(fragment.toLowerCase());
    }
}
