package de.unibielefeld.techfak.tdpe.prorema.locking;

import de.unibielefeld.techfak.tdpe.prorema.domain.Client;
import de.unibielefeld.techfak.tdpe.prorema.domain.Contact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
/**
 * @author Benedikt Volkmer
 *         Created on 5/22/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleLockServiceImplTest {

    SimpleLockServiceImpl simpleLockService = new SimpleLockServiceImpl();
    DomainIdentifier domainId0 = new DomainIdentifier(0, Client.class);
    DomainIdentifier domainId1 = new DomainIdentifier(1, Client.class);
    DomainIdentifier domainId2 = new DomainIdentifier(0, Contact.class);

    @Before
    public void setUp() {

    }


    @Test
    public void lockResource() {
        SimpleLock lock0 = simpleLockService.lockResource(domainId0, 0);
        SimpleLock lock1 = simpleLockService.lockResource(domainId1, 0);
        SimpleLock lock2 = simpleLockService.lockResource(domainId2, 0);
        SimpleLock lock0dup = simpleLockService.lockResource(domainId0, 0);


        Map<DomainIdentifier, SimpleLock> lockMap = simpleLockService.getLockMap();

        assertNotNull(lock0);
        assertNotNull(lock1);
        assertNotNull(lock2);
        assertNull(lock0dup);
        assertTrue(lockMap.containsKey(domainId0) && lockMap.get(domainId0).equals(lock0));
        assertTrue(lockMap.containsKey(domainId1) && lockMap.get(domainId1).equals(lock1));
        assertTrue(lockMap.containsKey(domainId2) && lockMap.get(domainId2).equals(lock2));
    }

    @Test
    public void releaseLock() {
        SimpleLock lock0 = simpleLockService.lockResource(domainId0, 0);
        SimpleLock lock1 = simpleLockService.lockResource(domainId0, 0);
        SimpleLock lock2 = simpleLockService.lockResource(domainId0, 0);

        simpleLockService.releaseLock(domainId0);
        Map<DomainIdentifier, SimpleLock> lockMap = simpleLockService.getLockMap();
        assertFalse(lockMap.containsKey(domainId0));

        simpleLockService.releaseLock(domainId1);
        lockMap = simpleLockService.getLockMap();
        assertFalse(lockMap.containsKey(domainId1));

        simpleLockService.releaseLock(domainId2);
        lockMap = simpleLockService.getLockMap();
        assertFalse(lockMap.containsKey(domainId2));
        assertTrue(lockMap.isEmpty());
    }

    @Test
    public void getLock() {
        SimpleLock lock0 = simpleLockService.lockResource(domainId0, 0);
        SimpleLock lock1 = simpleLockService.lockResource(domainId0, 0);

        SimpleLock lock0Get = simpleLockService.getLock(domainId0);
        assertEquals(lock0, lock0Get);

        SimpleLock lock1Get = simpleLockService.getLock(domainId1);
        assertEquals(lock1, lock1Get);

        SimpleLock lock2get = simpleLockService.getLock(domainId2);
        assertNull(lock2get);
    }

    @Test
    public void cleanupScheduler() throws Exception {
        SimpleLock lock0 = simpleLockService.lockResource(domainId0, 0);
        SimpleLock lock1 = simpleLockService.lockResource(domainId1, 0);

        lock1.setBeginDateTime(LocalDateTime.now().minusDays(1));

        simpleLockService.cleanupScheduler();

        assertThat(simpleLockService.getLock(domainId0)).isEqualTo(lock0);
        assertThat(simpleLockService.getLock(domainId1)).isNull();

    }

}