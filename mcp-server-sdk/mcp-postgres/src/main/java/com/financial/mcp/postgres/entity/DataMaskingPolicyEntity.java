package com.financial.mcp.postgres.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "data_masking_policy", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tool_id", "version"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataMaskingPolicyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String toolId;

    @Column(nullable = true)
    private String version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> columnMasks;

    @Column(nullable = false)
    private String dataLevel;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long updatedAt;
}
