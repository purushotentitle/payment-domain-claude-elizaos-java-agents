package com.example.payments.api;

import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "anthropic.api-key=",
        "payments.api-key="
})
@AutoConfigureMockMvc
class AgentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listsRegisteredAgents() throws Exception {
        mockMvc.perform(get("/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void invokesFraudAgentAndCreatesApprovalIds() throws Exception {
        AgentRequest request = new AgentRequest(
                "Evaluate before capture. New device, AVS fail, high value order.",
                "pay_123",
                new BigDecimal("2499.00"),
                "USD",
                "mrc_high_value",
                "cust_new_device",
                Map.of("cardNumber", "4111111111111111")
        );

        String json = mockMvc.perform(post("/agents/fraud-risk/invoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value("fraud-risk"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AgentResponse response = objectMapper.readValue(json, AgentResponse.class);

        assertThat(response.riskLevel()).isEqualTo("HIGH");
        assertThat(response.actions())
                .filteredOn(action -> action.requiresApproval())
                .allSatisfy(action -> assertThat(action.approvalId()).startsWith("apr_"));
    }

    @Test
    void returnsValidationErrorForMissingAmount() throws Exception {
        mockMvc.perform(post("/agents/fraud-risk/invoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Review this payment",
                                  "currency": "USD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"));
    }
}
