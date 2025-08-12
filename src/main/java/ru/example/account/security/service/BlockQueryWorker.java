package ru.example.account.security.service;

/**
 * Worker (Query Side of CQRS).
 * Responsible for all read-only operations to check the status of IP blocks.
 * Methods in this interface should not modify any data.
 */
public interface BlockQueryWorker {
    /**
     * Checks if a specific IP address is currently under an active block.
     * An active block is one that exists and has not yet expired.
     *
     * @param ipAddress The IP address to check.
     * @return true if the IP is actively blocked, false otherwise.
     */
    boolean isIpBlocked(String ipAddress);
}
