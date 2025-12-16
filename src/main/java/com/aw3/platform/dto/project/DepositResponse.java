package com.aw3.platform.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Deposit Response DTO
 * 
 * POST /api/project/wallet/deposit response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepositResponse {

    private String depositAddress;
    private BigDecimal amount;
    private String token;
    private String transactionId;
    private String status;
    private Long expiresAt;
}

