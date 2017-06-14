package de.unibielefeld.techfak.tdpe.prorema.locking;

/**
 * @author Benedikt Volkmer
 *         Created on 5/19/16.
 */
public interface SimpleLockService {

    /**
     * Check if the domain resource is locked.
     *
     * @param domainIdentifier Identifying the domain
     * @return True if locked, false if not
     */
    SimpleLock getLock(DomainIdentifier domainIdentifier);

    /**
     * Lock the domain resource.
     *
     * @param domainIdentifier Identifying the domain
     * @param holderId         Id of the employee holding the lock
     * @return SimpleLock instance or null if not successive.
     */
    SimpleLock lockResource(DomainIdentifier domainIdentifier, Integer holderId);

    /**
     * Release the lock on the given domain resource.
     *
     * @param domainIdentifier Identifying the domain
     */
    void releaseLock(DomainIdentifier domainIdentifier);

}
