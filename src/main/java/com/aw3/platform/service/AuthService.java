package com.aw3.platform.service;

import com.aw3.platform.controller.auth.AuthController.*;
import com.aw3.platform.entity.User;
import com.aw3.platform.entity.enums.Role;
import com.aw3.platform.exception.BadRequestException;
import com.aw3.platform.exception.UnauthorizedException;
import com.aw3.platform.repository.UserRepository;
import com.aw3.platform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication Service
 * 
 * Handles wallet-based authentication using signature verification
 * 
 * Business Rules:
 * - Nonce expires after 5 minutes
 * - Each nonce can only be used once
 * - Wallet address must be valid Ethereum address format
 * - Signature verification uses EIP-191 or EIP-712
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final Web3Service web3Service;

    // In production, use Redis for nonce storage
    private final Map<String, NonceEntry> nonceStore = new ConcurrentHashMap<>();

    private static final long NONCE_EXPIRY_SECONDS = 300; // 5 minutes
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public WalletConnectResponse initiateWalletConnect(WalletConnectRequest request) {
        String walletAddress = request.getWalletAddress().toLowerCase();

        // Validate wallet address format
        if (!isValidWalletAddress(walletAddress)) {
            throw new BadRequestException("Invalid wallet address format");
        }

        // Generate nonce
        String nonce = generateNonce();
        String message = buildSignMessage(walletAddress, nonce);
        long expiresAt = System.currentTimeMillis() + (NONCE_EXPIRY_SECONDS * 1000);

        // Store nonce
        nonceStore.put(walletAddress, new NonceEntry(nonce, expiresAt));

        return WalletConnectResponse.builder()
                .walletAddress(walletAddress)
                .nonce(nonce)
                .message(message)
                .expiresAt(expiresAt)
                .build();
    }

    @Transactional
    public AuthResponse verifySignature(VerifySignatureRequest request) {
        String walletAddress = request.getWalletAddress().toLowerCase();

        // Verify nonce exists and is valid
        NonceEntry nonceEntry = nonceStore.get(walletAddress);
        if (nonceEntry == null) {
            throw new UnauthorizedException("No pending authentication request for this wallet");
        }

        if (System.currentTimeMillis() > nonceEntry.expiresAt) {
            nonceStore.remove(walletAddress);
            throw new UnauthorizedException("Authentication request expired");
        }

        if (!nonceEntry.nonce.equals(request.getNonce())) {
            throw new UnauthorizedException("Invalid nonce");
        }

        // Verify signature
        String message = buildSignMessage(walletAddress, request.getNonce());
        boolean isValid = web3Service.verifySignature(walletAddress, message, request.getSignature());

        if (!isValid) {
            throw new UnauthorizedException("Invalid signature");
        }

        // Remove used nonce
        nonceStore.remove(walletAddress);

        // Find or create user
        User user = userRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new UnauthorizedException("User not registered. Please register first."));

        // Update last login
        user.setLastLogin(Instant.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .user(UserInfo.builder()
                        .userId(user.getUserId())
                        .walletAddress(user.getWalletAddress())
                        .role(user.getRole().name())
                        .displayName(user.getDisplayName())
                        .profileComplete(isProfileComplete(user))
                        .subscriptionTier(user.getSubscriptionTier())
                        .build())
                .build();
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromRefreshToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .build();
    }

    public LogoutResponse logout(String authHeader) {
        // In production, add token to blacklist in Redis
        log.info("User logged out");
        return LogoutResponse.builder()
                .success(true)
                .message("Successfully logged out")
                .build();
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String walletAddress = request.getWalletAddress().toLowerCase();

        // Validate wallet address
        if (!isValidWalletAddress(walletAddress)) {
            throw new BadRequestException("Invalid wallet address format");
        }

        // Check if user already exists
        if (userRepository.findByWalletAddress(walletAddress).isPresent()) {
            throw new BadRequestException("Wallet already registered");
        }

        // Verify signature
        NonceEntry nonceEntry = nonceStore.get(walletAddress);
        if (nonceEntry == null || !nonceEntry.nonce.equals(request.getNonce())) {
            throw new UnauthorizedException("Invalid or expired nonce");
        }

        String message = buildSignMessage(walletAddress, request.getNonce());
        boolean isValid = web3Service.verifySignature(walletAddress, message, request.getSignature());

        if (!isValid) {
            throw new UnauthorizedException("Invalid signature");
        }

        // Validate terms acceptance
        if (!Boolean.TRUE.equals(request.getTermsAccepted())) {
            throw new BadRequestException("Terms must be accepted");
        }

        // Create user
        Role role = Role.valueOf(request.getRole().toUpperCase());
        User user = User.builder()
                .walletAddress(walletAddress)
                .role(role)
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .reputationScore(BigDecimal.valueOf(50)) // Initial reputation
                .subscriptionTier("FREE")
                .termsAcceptedAt(Instant.now())
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build();

        user = userRepository.save(user);
        nonceStore.remove(walletAddress);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        List<String> nextSteps = getNextStepsForRole(role);

        return RegistrationResponse.builder()
                .userId(user.getUserId())
                .walletAddress(user.getWalletAddress())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .profileComplete(false)
                .nextSteps(nextSteps)
                .build();
    }

    public NonceResponse getNonce(String walletAddress) {
        walletAddress = walletAddress.toLowerCase();

        if (!isValidWalletAddress(walletAddress)) {
            throw new BadRequestException("Invalid wallet address format");
        }

        String nonce = generateNonce();
        String message = buildSignMessage(walletAddress, nonce);
        long expiresAt = System.currentTimeMillis() + (NONCE_EXPIRY_SECONDS * 1000);

        nonceStore.put(walletAddress, new NonceEntry(nonce, expiresAt));

        return NonceResponse.builder()
                .walletAddress(walletAddress)
                .nonce(nonce)
                .message(message)
                .expiresAt(expiresAt)
                .build();
    }

    // Helper methods

    private String generateNonce() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String buildSignMessage(String walletAddress, String nonce) {
        return String.format(
            "Welcome to AW3 Platform!\n\n" +
            "Please sign this message to authenticate.\n\n" +
            "Wallet: %s\n" +
            "Nonce: %s\n\n" +
            "This will not trigger a blockchain transaction.",
            walletAddress, nonce
        );
    }

    private boolean isValidWalletAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }

    private boolean isProfileComplete(User user) {
        return user.getDisplayName() != null && !user.getDisplayName().isEmpty();
    }

    private List<String> getNextStepsForRole(Role role) {
        return switch (role) {
            case CREATOR -> List.of(
                "Complete your profile with social links",
                "Verify your social accounts",
                "Browse available campaigns"
            );
            case PROJECT -> List.of(
                "Complete your project profile",
                "Fund your wallet",
                "Create your first campaign"
            );
            case VALIDATOR -> List.of(
                "Complete validator requirements",
                "Stake tokens to become eligible",
                "Review available verification tasks"
            );
            default -> List.of("Explore the platform");
        };
    }

    private static class NonceEntry {
        final String nonce;
        final long expiresAt;

        NonceEntry(String nonce, long expiresAt) {
            this.nonce = nonce;
            this.expiresAt = expiresAt;
        }
    }
}

