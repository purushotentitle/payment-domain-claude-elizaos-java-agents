package com.example.payments.api;

import com.example.payments.domain.AgentRequest;
import com.example.payments.orchestration.PaymentReviewStartResponse;
import com.example.payments.orchestration.TemporalPaymentReviewLauncher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrations")
@Tag(name = "Orchestration", description = "Durable workflow orchestration for payment agent reviews")
public class OrchestrationController {
    private final ObjectProvider<TemporalPaymentReviewLauncher> temporalLauncher;

    public OrchestrationController(ObjectProvider<TemporalPaymentReviewLauncher> temporalLauncher) {
        this.temporalLauncher = temporalLauncher;
    }

    @PostMapping("/payment-review/{agentId}")
    @Operation(summary = "Start a durable Temporal payment-review workflow")
    public ResponseEntity<PaymentReviewStartResponse> startPaymentReview(
            @PathVariable("agentId") String agentId,
            @Valid @RequestBody AgentRequest request
    ) {
        TemporalPaymentReviewLauncher launcher = temporalLauncher.getIfAvailable();
        if (launcher == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(PaymentReviewStartResponse.temporalDisabled());
        }
        return ResponseEntity.accepted().body(launcher.start(agentId, request));
    }
}
