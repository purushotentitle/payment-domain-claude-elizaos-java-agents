package com.example.payments.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "anthropic.api-key=",
        "payments.api-key=test-key"
})
@AutoConfigureMockMvc
class ApiKeySecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsProtectedEndpointWithoutApiKey() throws Exception {
        mockMvc.perform(get("/agents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsProtectedEndpointWithApiKey() throws Exception {
        mockMvc.perform(get("/agents").header("X-API-Key", "test-key"))
                .andExpect(status().isOk());
    }

    @Test
    void keepsHealthEndpointPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }
}
