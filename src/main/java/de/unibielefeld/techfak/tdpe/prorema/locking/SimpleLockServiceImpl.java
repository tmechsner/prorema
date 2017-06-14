package de.unibielefeld.techfak.tdpe.prorema.locking;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Service handling and organising locks.
 *
 * @author Benedikt Volkmer
 *         Created on 5/19/16.
 */
@Log4j2
@NoArgsConstructor
@Service
@Scope(scopeName = "singleton") //Implicit set by default but explicit set to ensure
public class SimpleLockServiceImpl implements SimpleLockService {

    @Getter
    private final Map<DomainIdentifier, SimpleLock> lockMap = new HashMap<>();

    @Override
    public SimpleLock getLock(DomainIdentifier domainIdentifier) {
        if (lockMap.containsKey(domainIdentifier)) {
            return lockMap.get(domainIdentifier);
        }
        return null;
    }

    @Override
    public SimpleLock lockResource(DomainIdentifier domainIdentifier, Integer holderId) {
        if (getLock(domainIdentifier) == null) {
            SimpleLock lock = new SimpleLock(holderId, LocalDateTime.now());
            lockMap.put(domainIdentifier, lock);
            return lock;
        } else {
            return null;
        }
    }

    @Override
    public void releaseLock(DomainIdentifier domainIdentifier) {
        lockMap.remove(domainIdentifier);
    }

    /**
     * Scheduler which cleans up expired locks.
     */
    @Scheduled(cron = "0 0 2 * * *")//Run every morning at 2am
    public void cleanupScheduler() {
        List<DomainIdentifier> toRemove = new LinkedList<>();
        lockMap.forEach((di, sl) -> {
            if (sl.getBeginDateTime().plus(sl.getDuration()).compareTo(LocalDateTime.now()) < 0) {
                toRemove.add(di);
            }
        });
        toRemove.forEach(di -> {
            lockMap.remove(di);
            log.debug("Removing lock for " + di.getDomainClass()
                      + " with id " + di.getDomainId() + " as it is expired.");
        });
    }
}
