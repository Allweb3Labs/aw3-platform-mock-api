package com.aw3.platform.repository;

import com.aw3.platform.entity.User;
import com.aw3.platform.entity.enums.UserRole;
import com.aw3.platform.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, java.util.UUID> {

    Optional<User> findByWalletAddress(String walletAddress);

    Optional<User> findByDidIdentifier(String didIdentifier);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByWalletAddress(String walletAddress);

    boolean existsByDidIdentifier(String didIdentifier);

    boolean existsByEmail(String email);

    Page<User> findByUserRole(UserRole userRole, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByUserRoleAndStatus(UserRole userRole, UserStatus status, Pageable pageable);

    Long countByUserRole(UserRole userRole);

    Long countByStatus(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.userRole = :role AND u.status = :status")
    Page<User> findByRoleAndStatus(@Param("role") UserRole role, 
                                   @Param("status") UserStatus status, 
                                   Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.displayName LIKE %:searchTerm%")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}

