package com.dormsupport.auth.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByIdAndRevokedAtIsNullAndExpiresAtAfter(UUID id, Instant now);
}
