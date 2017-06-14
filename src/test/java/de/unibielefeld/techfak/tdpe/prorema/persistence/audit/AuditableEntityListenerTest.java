package de.unibielefeld.techfak.tdpe.prorema.persistence.audit;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ChangelogEntryRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by timo on 31.05.16.
 */
public class AuditableEntityListenerTest {

    private static Employee user = new Employee("Title", "Tim", "Toaster", "", "", "", "", "", "",
                                                Employee.Position.CONSULTANT, 50, LocalDate.now().minusYears(5),
                                                LocalDate.now().plusYears(3), "tim", "1234");


    private EmployeeEntity entity;
    private String title = "Title",
            firstName = "Tim",
            lastName = "Toaster",
            position = "MANAGER";
    private int workSchedule = 50;
    private LocalDate workEntry = LocalDate.now().minusYears(5);
    private LocalDate workExit = LocalDate.now().plusYears(3);

    /**
     * Test Subject
     */
    private AuditableEntityListener listener = new AuditableEntityListener();

    /**
     * Setup mocks
     */
    @BeforeClass
    public static void setUp() {
        EntityObserver.setRepository(Mockito.mock(ChangelogEntryRepository.class));

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);

        user.setId(1);

        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    /**
     * Create new entity to clear changelog
     *
     * @throws Exception
     */
    @Before
    public void createEntity() throws Exception {
        entity = new EmployeeEntity(title, firstName, lastName, position, "", "", workEntry, workExit, "",
                                    "", "", "", workSchedule, "username", "abc", null);
    }

    @Test
    public void testPostLoad() throws Exception {
        listener.postLoad(entity);

        Map<String, Object> loadedValues = entity.getLoadedValues();
        assertEquals(19, loadedValues.size());
        assertEquals(null, loadedValues.get("id"));
        assertEquals(null, loadedValues.get("organizationUnit"));
        assertEquals(null, loadedValues.get("mail"));
        assertEquals("", loadedValues.get("tel"));
        assertEquals("", loadedValues.get("zip"));
        assertEquals("", loadedValues.get("street"));
        assertEquals("", loadedValues.get("city"));
        assertEquals("", loadedValues.get("country"));
        assertEquals(null, loadedValues.get("project"));
        assertEquals(null, loadedValues.get("employeeProfile"));
        assertEquals(title, loadedValues.get("nameTitle"));
        assertEquals(firstName, loadedValues.get("firstName"));
        assertEquals(lastName, loadedValues.get("lastName"));
        assertEquals(position, loadedValues.get("position"));
        assertEquals(workSchedule, loadedValues.get("workschedule"));
        assertEquals(workEntry, loadedValues.get("workEntry"));
        assertEquals(workExit, loadedValues.get("workExit"));
        assertEquals(null, loadedValues.get("hasSkills"));
        assertEquals(null, loadedValues.get("employeeProfiles"));
        assertEquals(null, loadedValues.get("managerProjectIds"));
        assertEquals(null, loadedValues.get("firstManagerOrganisationIds"));
        assertEquals(null, loadedValues.get("secondManagerOrganisationIds"));
    }

    @Test
    public void testPreRemove() throws Exception {
        listener.preRemove(entity);

        List<ChangelogEntryEntity> changelogEntries = new LinkedList<>(entity.getChangelogEntries());
        assertEquals(1, changelogEntries.size());

        ChangelogEntryEntity changelogEntry = changelogEntries.get(0);
        assertEquals(ChangelogEntryEntity.ChangeType.DELETED, changelogEntry.getChangeType());
        assertEquals(null, changelogEntry.getFieldName());
        assertEquals(null, changelogEntry.getNewValue());
        assertEquals(null, changelogEntry.getOldValue());
        assertEquals(LocalDate.now(), changelogEntry.getTimestamp());
        assertEquals(user.getId(), changelogEntry.getUserId());
    }

    @Test
    public void testPrePersist() throws Exception {
        listener.prePersist(entity);

        List<ChangelogEntryEntity> changelogEntries = new LinkedList<>(entity.getChangelogEntries());
        assertEquals(1, changelogEntries.size());

        ChangelogEntryEntity changelogEntry = changelogEntries.get(0);
        assertEquals(ChangelogEntryEntity.ChangeType.CREATED, changelogEntry.getChangeType());
        assertEquals(null, changelogEntry.getFieldName());
        assertEquals(null, changelogEntry.getNewValue());
        assertEquals(null, changelogEntry.getOldValue());
        assertEquals(LocalDate.now(), changelogEntry.getTimestamp());
        assertEquals(user.getId(), changelogEntry.getUserId());
    }

    @Test
    public void testPreUpdate() throws Exception {

        // Setup value list
        listener.postLoad(entity);

        // Change entity
        LocalDate newWorkExit = LocalDate.now().plusYears(10);

        String newPosition = "CONSULTANT";

        entity.setPassword("asd");
        // entity.setEmployeeProfiles(employeeProfiles);
        entity.setWorkExit(newWorkExit);
        entity.setPosition(newPosition);
        entity.setId(1);

        // Simulate persist action
        listener.preUpdate(entity);

        Set<ChangelogEntryEntity> changelogEntries = new HashSet<>(entity.getChangelogEntries());
        assertEquals(2, changelogEntries.size());

        ChangelogEntryEntity changelogEntryWorkExit = new ChangelogEntryEntity();
        changelogEntryWorkExit.setChangeType(ChangelogEntryEntity.ChangeType.UPDATED);
        changelogEntryWorkExit.setFieldName("workExit");
        changelogEntryWorkExit.setOldValue(workExit.toString());
        changelogEntryWorkExit.setOldValue(newWorkExit.toString());
        changelogEntryWorkExit.setTimestamp(LocalDate.now());
        changelogEntryWorkExit.setUserId(entity.getId());

        ChangelogEntryEntity changelogEntryPosition = new ChangelogEntryEntity();
        changelogEntryPosition.setChangeType(ChangelogEntryEntity.ChangeType.UPDATED);
        changelogEntryPosition.setFieldName("position");
        changelogEntryPosition.setOldValue(position);
        changelogEntryPosition.setOldValue(newPosition);
        changelogEntryPosition.setTimestamp(LocalDate.now());
        changelogEntryPosition.setUserId(entity.getId());
    }
}