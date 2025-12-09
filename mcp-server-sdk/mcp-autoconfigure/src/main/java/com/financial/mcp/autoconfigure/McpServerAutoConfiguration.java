package com.financial.mcp.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.audit.AuditRepository;
import com.financial.mcp.core.audit.AuditService;
import com.financial.mcp.core.idempotency.IdempotencyRepository;
import com.financial.mcp.core.idempotency.IdempotencyService;
import com.financial.mcp.core.killswitch.KillSwitchRepository;
import com.financial.mcp.core.killswitch.KillSwitchService;
import com.financial.mcp.core.masking.DataMaskingService;
import com.financial.mcp.core.policy.PolicyRepository;
import com.financial.mcp.core.policy.PolicyService;
import com.financial.mcp.core.registry.ToolRegistryRepository;
import com.financial.mcp.core.registry.ToolRegistryService;
import com.financial.mcp.core.rpc.JsonRpcDispatcher;
import com.financial.mcp.core.rpc.JsonRpcHandler;
import com.financial.mcp.core.validation.JsonSchemaValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class McpServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonSchemaValidator jsonSchemaValidator(ObjectMapper objectMapper) {
        return new JsonSchemaValidator(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolRegistryService toolRegistryService(ToolRegistryRepository repository) {
        return new ToolRegistryService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public PolicyService policyService(PolicyRepository repository) {
        return new PolicyService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public KillSwitchService killSwitchService(KillSwitchRepository repository) {
        return new KillSwitchService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataMaskingService dataMaskingService(ObjectMapper objectMapper) {
        return new DataMaskingService(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditService auditService(AuditRepository repository, ObjectMapper objectMapper) {
        return new AuditService(repository, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyService idempotencyService(IdempotencyRepository repository) {
        return new IdempotencyService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonRpcDispatcher jsonRpcDispatcher(
            ToolRegistryService toolRegistryService,
            PolicyService policyService,
            KillSwitchService killSwitchService,
            JsonSchemaValidator schemaValidator,
            DataMaskingService maskingService,
            AuditService auditService,
            IdempotencyService idempotencyService,
            ObjectMapper objectMapper,
            JsonRpcHandler handler) {
        return new JsonRpcDispatcher(
                toolRegistryService,
                policyService,
                killSwitchService,
                schemaValidator,
                maskingService,
                auditService,
                idempotencyService,
                objectMapper,
                handler
        );
    }
}
