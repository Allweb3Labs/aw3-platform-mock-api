package com.aw3.platform.controller.project;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.project.FeeEstimateRequest;
import com.aw3.platform.dto.project.FeeEstimateResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.FeeCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Project Portal - Financial Management Endpoints
 * 
 * Fee calculation and estimation endpoints
 */
@RestController
@RequestMapping("/project/finance")
@PreAuthorize("hasRole('PROJECT')")
@RequiredArgsConstructor
@Slf4j
public class ProjectFinanceController {

    private final FeeCalculationService feeCalculationService;

    /**
     * POST /api/project/finance/estimate-fees
     * Calculate estimated service & oracle fees
     */
    @PostMapping("/estimate-fees")
    public ApiResponse<FeeEstimateResponse> estimateFees(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FeeEstimateRequest request) {
        
        log.info("Project {} estimating fees for budget: {}", 
                userDetails.getUserId(), request.getCampaignBudget());

        FeeEstimateResponse estimate = feeCalculationService.calculateCampaignFees(
                userDetails.getUserId(), 
                request
        );

        return ApiResponse.success(estimate);
    }
}

