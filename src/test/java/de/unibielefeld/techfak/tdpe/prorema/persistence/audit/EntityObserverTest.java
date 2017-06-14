package de.unibielefeld.techfak.tdpe.prorema.persistence.audit;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ContactEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by timo on 24.05.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityObserverTest {

    private static Employee user = new Employee("Title", "Tim", "Toaster", "", "", "", "", "", "",
                                                Employee.Position.CONSULTANT, 50, LocalDate.now().minusYears(5),
                                                LocalDate.now().plusYears(3), "tim", "1234");

    /**
     * Test Entity
     */
    private AuditableEntity entity;

    /**
     * Setup mocks
     */
    @BeforeClass
    public static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        user.setId(1);

        SecurityContextHolder.setContext(securityContext);

        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    /**
     * Reset test entity
     */
    @Before
    public void createEntity() {
        entity = new ContactEntity("Title", "Tom", "Tanne", "", "", "", "", "", "");
    }

    @Test
    public void testNotifyCreation() throws Exception {
        EntityObserver.notifyCreation(entity);

        List<ChangelogEntryEntity> changelogEntries = new LinkedList<>(entity.getChangelogEntries());
        Assert.assertNotNull(changelogEntries);
        Assert.assertEquals(changelogEntries.size(), 1);

        ChangelogEntryEntity entry = changelogEntries.get(0);
        Assert.assertEquals(entry.getChangeType(), ChangelogEntryEntity.ChangeType.CREATED);
        Assert.assertEquals(entry.getTimestamp(), LocalDate.now());
        Assert.assertEquals(entry.getUserId(), user.getId());
    }

    @Test
    public void testNotifyUpdate() throws Exception {
        EntityObserver.notifyUpdate(entity, "Vorname", "Tom", "Günther");

        List<ChangelogEntryEntity> changelogEntries = new LinkedList<>(entity.getChangelogEntries());
        Assert.assertNotNull(changelogEntries);
        Assert.assertEquals(changelogEntries.size(), 1);

        ChangelogEntryEntity entry = changelogEntries.get(0);
        Assert.assertEquals(entry.getChangeType(), ChangelogEntryEntity.ChangeType.UPDATED);
        Assert.assertEquals(entry.getTimestamp(), LocalDate.now());
        Assert.assertEquals(entry.getUserId(), user.getId());
        Assert.assertEquals(entry.getFieldName(), "Vorname");
        Assert.assertEquals(entry.getNewValue(), "Günther");
        Assert.assertEquals(entry.getOldValue(), "Tom");
    }

    @Test
    public void testNotifyDeletion() throws Exception {
        EntityObserver.notifyDeletion(entity);

        List<ChangelogEntryEntity> changelogEntries = new LinkedList<>(entity.getChangelogEntries());
        Assert.assertNotNull(changelogEntries);
        Assert.assertEquals(changelogEntries.size(), 1);

        ChangelogEntryEntity entry = changelogEntries.get(0);
        Assert.assertEquals(entry.getChangeType(), ChangelogEntryEntity.ChangeType.DELETED);
        Assert.assertEquals(entry.getTimestamp(), LocalDate.now());
        Assert.assertEquals(entry.getUserId(), user.getId());
    }
}