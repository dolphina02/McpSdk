package com.financial.mcp.postgres.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tool_policy", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tool_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolPolicyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String toolId;

    @Column(nullable = false)
    private boolean allowed;

    @Column(nullable = false)
    private String dataLevel;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long updatedAt;
}
