package com.aw3.platform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication request DTO for wallet-based login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Wallet address is required")
    private String walletAddress;

    @NotBlank(message = "Signature is required")
    private String signature;

    private String message;
}

