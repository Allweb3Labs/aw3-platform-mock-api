package com.aw3.platform.controller.creator;

import com.aw3.platform.entity.Application;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.enums.ApplicationStatus;
import com.aw3.platform.entity.enums.CampaignStatus;
import com.aw3.platform.repository.ApplicationRepository;
import com.aw3.platform.repository.CampaignRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CreatorApplicationController
 * 
 * Tests cover:
 * - Application submission
 * - Ownership validation
 * - Duplicate prevention
 * - Campaign status checks
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Creator Application Controller Integration Tests")
class CreatorApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private CampaignRepository campaignRepository;

    private UUID campaignId;
    private UUID creatorId;
    private Campaign activeCampaign;

    @BeforeEach
    void setUp() {
        campaignId = UUID.randomUUID();
        creatorId = UUID.randomUUID();

        activeCampaign = Campaign.builder()
                .campaignId(campaignId)
                .projectId(UUID.randomUUID())
                .title("Test Campaign")
                .description("Test Description")
                .category("DeFi")
                .budgetAmount(new BigDecimal("10000"))
                .budgetToken("USDC")
                .status(CampaignStatus.ACTIVE)
                .deadline(Instant.now().plusSeconds(30 * 24 * 60 * 60))
                .build();
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    @DisplayName("Should successfully submit application to active campaign")
    void testSubmitApplication_Success() throws Exception {
        // Given
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(activeCampaign));
        when(applicationRepository.existsByCampaignIdAndCreatorId(any(), any())).thenReturn(false);
        when(applicationRepository.save(any())).thenAnswer(invocation -> {
            Application app = invocation.getArgument(0);
            app.setApplicationId(UUID.randomUUID());
            return app;
        });

        String requestBody = """
                {
                    "campaignId": "%s",
                    "proposedRate": 5000,
                    "proposal": "I am interested in this campaign",
                    "portfolioLinks": ["https://twitter.com/creator"],
                    "relevantExperience": "5 years in crypto marketing",
                    "estimatedCompletionDays": 14
                }
                """.formatted(campaignId);

        // When & Then
        mockMvc.perform(post("/creator/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    @DisplayName("Should reject application to inactive campaign")
    void testSubmitApplication_InactiveCampaign() throws Exception {
        // Given
        Campaign inactiveCampaign = Campaign.builder()
                .campaignId(campaignId)
                .projectId(UUID.randomUUID())
                .title("Inactive Campaign")
                .status(CampaignStatus.COMPLETED)
                .build();

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(inactiveCampaign));

        String requestBody = """
                {
                    "campaignId": "%s",
                    "proposedRate": 5000,
                    "proposal": "I am interested in this campaign"
                }
                """.formatted(campaignId);

        // When & Then
        mockMvc.perform(post("/creator/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    @DisplayName("Should reject duplicate application")
    void testSubmitApplication_Duplicate() throws Exception {
        // Given
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(activeCampaign));
        when(applicationRepository.existsByCampaignIdAndCreatorId(any(), any())).thenReturn(true);

        String requestBody = """
                {
                    "campaignId": "%s",
                    "proposedRate": 5000,
                    "proposal": "I am interested in this campaign"
                }
                """.formatted(campaignId);

        // When & Then
        mockMvc.perform(post("/creator/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject unauthenticated request")
    void testSubmitApplication_Unauthenticated() throws Exception {
        // Given
        String requestBody = """
                {
                    "campaignId": "%s",
                    "proposedRate": 5000,
                    "proposal": "I am interested in this campaign"
                }
                """.formatted(campaignId);

        // When & Then
        mockMvc.perform(post("/creator/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}

