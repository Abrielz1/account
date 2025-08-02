package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrustedFingerprintRepository extends JpaRepository<TrustedFingerprintRepository, Long> {

    @Query(value = """
            SELECT EXISTS(SELECT 1 FROM security.trusted_fingerprints AS tf
            WHERE tf.profile_user_id = :userId
             AND tf.fingerprint = :fingerprint
              AND tf.is_trusted = true)
              """,
            nativeQuery = true)
    boolean isFingerprintTrustedForUser(
            @Param("userId") Long userId,
            @Param("fingerprint") String fingerprint);
}
