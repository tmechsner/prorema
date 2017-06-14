package de.unibielefeld.techfak.tdpe.prorema.locking;

import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by Martin
 */
@NoArgsConstructor
@Log4j2
@Service
public class WorksOnLockServiceImpl implements WorksOnLockService {

    @Getter
    private final Map<Integer, WorksOnLock> lockMap = new HashMap<>();

    @Override
    public WorksOnLock getLock(Integer orgaId) {
        if (lockMap.containsKey(orgaId)) {
            return lockMap.get(orgaId);
        }
        return null;
    }

    @Override
    public WorksOnLock lockResource(Integer userId, Integer orgaId) {
        if (getLock(orgaId) == null) {
            WorksOnLock lock = new WorksOnLock(userId ,orgaId, LocalDateTime.now());
            lockMap.put(orgaId, lock);
            return lock;
        } else {
            return null;
        }
    }

    @Override
    public void releaseLock(Integer orgaId) {
        lockMap.remove(orgaId);
    }

    /**
     * Scheduler which cleans up expired locks.
     */
    @Scheduled(cron = "0 0 2 * * *")//Run every morning at 2am
    public void cleanupScheduler() {
        List<Integer> toRemove = new LinkedList<>();
        lockMap.forEach((di, sl) -> {
            if (sl.getBeginDateTime().plus(sl.getDuration()).compareTo(LocalDateTime.now()) < 0) {
                toRemove.add(di);
            }
        });
        toRemove.forEach(di -> {
            lockMap.remove(di);
            log.debug("Removing lock with id " + di);
        });
    }

}
