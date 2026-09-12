package com.example.payments.api;

import com.example.payments.domain.AgentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "anthropic.api-key=",
        "payments.api-key=",
        "temporal.client.enabled=false",
        "temporal.worker.enabled=false"
})
@AutoConfigureMockMvc
class OrchestrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void returnsDisabledResponseWhenTemporalClientIsNotEnabled() throws Exception {
        AgentRequest request = new AgentRequest(
                "Evaluate this card payment before capture.",
                "pay_123",
                BigDecimal.valueOf(100),
                "USD",
                "mrc_demo",
                "cust_demo",
                Map.of()
        );

        mockMvc.perform(post("/orchestrations/payment-review/fraud-risk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.orchestration").value("temporal"))
                .andExpect(jsonPath("$.status").value("DISABLED"));
    }
}
