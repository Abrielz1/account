package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.RegistrationRequest;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRegistrationRepository extends JpaRepository<RegistrationRequest, UUID> {

    Optional<RegistrationRequest> findByIdAndAndEmailHashAndVerificationToken(UUID id, String email, String token);
}
