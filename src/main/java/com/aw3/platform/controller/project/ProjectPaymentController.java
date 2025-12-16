package com.aw3.platform.controller.project;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Project Portal - Payment Management Endpoints
 * 
 * Responsibilities:
 * - Release payments to creators
 * - View escrow details
 * - Track payment transactions
 */
@RestController
@RequestMapping("/project/payments")
@PreAuthorize("hasRole('PROJECT')")
@RequiredArgsConstructor
@Slf4j
public class ProjectPaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/project/payments/release
     * Release escrowed payment to creator after deliverable verification
     */
    @PostMapping("/release")
    public ApiResponse<PaymentService.PaymentReleaseResponse> releasePayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PaymentReleaseRequest request) {
        
        log.info("Project {} releasing payment for deliverable {}", 
                userDetails.getUserId(), request.getDeliverableId());

        try {
            CompletableFuture<PaymentService.PaymentReleaseResponse> future = 
                    paymentService.releasePayment(
                            UUID.fromString(request.getDeliverableId()),
                            userDetails.getUserId()
                    );

            PaymentService.PaymentReleaseResponse response = future.get();
            
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Error releasing payment: {}", e.getMessage(), e);
            return ApiResponse.error("PAYMENT_ERROR", e.getMessage());
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentReleaseRequest {
        private String campaignId;
        private String deliverableId;
        private String releaseType; // full, partial, milestone
    }
}

