package com.financial.mcp.core.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataMaskingPolicy {
    private String userId;
    private String toolId;
    private String version; // Optional: for version-specific masking policies
    private Map<String, String> columnMasks; // column_name -> mask_type (HASH, REDACT, PARTIAL)
    private String dataLevel;
}
