package com.example.payments.api;

import com.example.payments.agents.AgentService;
import com.example.payments.domain.AgentDescriptor;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/agents")
@Tag(name = "Payment Agents", description = "Invoke payment-domain AI agents")
public class AgentController {
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    @Operation(summary = "List available payment agents")
    public List<AgentDescriptor> listAgents() {
        return agentService.listAgents();
    }

    @PostMapping("/{agentId}/invoke")
    @Operation(summary = "Invoke a payment agent")
    public AgentResponse invokeAgent(
            @PathVariable("agentId") String agentId,
            @Valid @RequestBody AgentRequest request
    ) {
        return agentService.invoke(agentId, request);
    }
}
