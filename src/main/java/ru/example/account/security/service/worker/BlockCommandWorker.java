package ru.example.account.security.service.worker;

import ru.example.account.security.entity.BlockReason;
import ru.example.account.security.entity.BlockedEntityType;
import ru.example.account.security.entity.BlockedTarget;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Worker (Command Side of CQRS).
 * Defines contract for all write and update operations related to blocking targets.
 * The primary implementation of this interface should ensure that each method
 * is executed in a new, independent transaction to guarantee atomicity.
 */
public interface BlockCommandWorker {

    /**
     * Blocks a target entity (e.g., an IP address, a User ID).
     * <p>
     * This operation is designed to be idempotent:
     * <ul>
     *     <li>If no active block exists for the given target, a new one is created.</li>
     *     <li>If a block already exists, its details (duration, reason) will be updated.</li>
     *     <li>If a "soft-deleted" block exists, it will be "undeleted" and updated.</li>
     * </ul>
     *
     *                                  The type of the target to be blocked (e.g., IP_ADDRESS).
     * @param targetValue                The actual value of the target (e.g., "192.168.1.100", or fingerprint).
     * @param duration             The duration for which the target should be blocked.
     *                             A null value implies a permanent block.
     * @param reason               A concise, machine-readable string describing the reason for the block in form of enum.
     * @param blockType            Valid reason to set block on attacker
     * @param triggeringIncidentId The optional UUID of the {@link ru.example.account.security.entity.SecurityIncident}
     *                             that triggered this blocking action, used for auditing purposes.
     * @param affectedUserid is id which under attack.
     * @param expiresAt time when ban is lifted, if == null ban is set permanent, default duration of blocking is 7 days
     * @return An {@link Optional} containing the persisted {@code BlockedTarget} entity if the
     *         operation was successful. Returns an empty Optional if the input parameters
     *         (e.g., type or value) were invalid.
     */
    Optional<BlockedTarget> blockTarget(
            String targetValue,
            Duration duration,
            BlockReason reason,
            BlockedEntityType blockType,
            Long affectedUserid,
            Long triggeringIncidentId,
            ZonedDateTime expiresAt
    );
}
