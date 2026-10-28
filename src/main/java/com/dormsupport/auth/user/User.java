package com.dormsupport.auth.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_login", columnNames = "login"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String login;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(length = 50)
    private String block;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected User() {
        // Required by JPA.
    }

    public User(String login, String passwordHash, String fullName, UserRole role,
                String block, String roomNumber) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.block = block;
        this.roomNumber = roomNumber;
    }

    /** Compatibility constructor for existing test fixtures. */
    public User(String login, String passwordHash, String fullName, UserRole role,
                UUID blockId, String roomNumber, Void... ignored) {
        this(login, passwordHash, fullName, role,
                blockId == null ? null : blockId.toString(), roomNumber);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (role == null) {
            role = UserRole.RESIDENT;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getBlock() {
        return block;
    }

    /** @deprecated use {@link #getBlock()} */
    @Deprecated
    public UUID getBlockId() {
        try {
            return block == null ? null : UUID.fromString(block);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public void setBlock(String block) {
        this.block = block;
    }

    /** @deprecated use {@link #setBlock(String)} */
    @Deprecated
    public void setBlockId(UUID blockId) {
        this.block = blockId == null ? null : blockId.toString();
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
