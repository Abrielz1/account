package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.SecurityIncident;

@Repository
public interface SecurityIncidentRepository extends JpaRepository<SecurityIncident, Long> {
}
