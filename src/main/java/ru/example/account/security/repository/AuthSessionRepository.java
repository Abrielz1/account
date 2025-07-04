package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.SessionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);

    List<AuthSession> findAllByUserIdAndStatus(Long userId, SessionStatus status);
}
