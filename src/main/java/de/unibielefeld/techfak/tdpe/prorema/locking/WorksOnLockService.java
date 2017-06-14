package de.unibielefeld.techfak.tdpe.prorema.locking;

/**
 * Created by Martin
 */
public interface WorksOnLockService {


    /**
     * Check if the domain resource is locked.
     *
     * @param orgaId
     * @return True if locked, false if not
     */
    WorksOnLock getLock(Integer orgaId);

    /**
     * Lock the domain resource.
     *
     * @param orgaId Identifying the domain
     * @return SimpleLock instance or null if not successive.
     */
    WorksOnLock lockResource(Integer userId, Integer orgaId);

    /**
     * Release the lock on the given domain resource.
     *
     * @param orgaId the domain
     */
    void releaseLock(Integer orgaId);

}
