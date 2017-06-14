package de.unibielefeld.techfak.tdpe.prorema.persistence.audit;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.*;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ChangelogEntryRepository;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;

/**
 * Creates Changelog Entries.
 * Created by timo on 24.05.16.
 */
@Service
public class EntityObserver {

    private static ChangelogEntryRepository repository;

    @Autowired
    private ChangelogEntryRepository repository0;

    @PostConstruct
    public void initStaticObserver () {
        repository = this.repository0;
    }

    public static void setRepository(ChangelogEntryRepository repository) {
        EntityObserver.repository = repository;
    }

    /**
     * The given entity has been created.
     *
     * @param entity The entity that has been created.
     */
    public static void notifyCreation(AuditableEntity entity) {
        Employee user = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        ChangelogEntryEntity entry = new ChangelogEntryEntity(entity, ChangelogEntryEntity.ChangeType.CREATED, LocalDate.now(),
                                                              user.getId());
        repository.save(entry);
        entity.addChangelogEntry(entry);
    }

    /**
     * The given entity has been updated.
     *
     * @param entity   The entity that has been updated.
     * @param field    The updated field.
     * @param oldValue The value before the update.
     * @param newValue The value after the update.
     */
    public static void notifyUpdate(AuditableEntity entity, String field, Object oldValue,
                             Object newValue) {
        Employee user = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Object oldVal = oldValue;
        Object newVal = newValue;

            if (oldValue.getClass().equals(String.class) && newValue.getClass().equals(String.class)) {
                if (oldValue.toString().toLowerCase().equals(newValue.toString().toLowerCase())) {
                    return;
                }
            }
            if (oldValue.getClass().equals(ClientEntity.class)) {
                ClientEntity clientEntity1 = (ClientEntity) oldValue;
                ClientEntity clientEntity2 = (ClientEntity) newValue;
                oldVal = clientEntity1.getName();
                newVal = clientEntity2.getName();
            } else if (oldValue.getClass().equals(ProjectEntity.class)) {
                ProjectEntity projectEntity1 = (ProjectEntity) oldValue;
                ProjectEntity projectEntity2 = (ProjectEntity) newValue;
                oldVal = projectEntity1.getName();
                newVal = projectEntity2.getName();
            } else if (oldValue.getClass().equals(SkillEntity.class)) {
                SkillEntity skillEntity1 = (SkillEntity) oldValue;
                SkillEntity skillEntity2 = (SkillEntity) newValue;
                oldVal = skillEntity1.getName();
                newVal = skillEntity2.getName();
            } else if (oldValue.getClass().equals(ContactEntity.class)) {
                ContactEntity contactEntity1 = (ContactEntity) oldValue;
                ContactEntity contactEntity2 = (ContactEntity) newValue;
                oldVal = contactEntity1.getFirstName() + " " + contactEntity1.getLastName();
                newVal = contactEntity2.getFirstName() + " " + contactEntity2.getLastName();
            } else if (oldValue.getClass().equals(OrganisationUnitEntity.class)) {
                OrganisationUnitEntity organisationUnitEntity1 = (OrganisationUnitEntity) oldValue;
                OrganisationUnitEntity organisationUnitEntity2 = (OrganisationUnitEntity) newValue;
                oldVal = organisationUnitEntity1.getName();
                newVal = organisationUnitEntity2.getName();
            } else if (oldValue.getClass().equals(EmployeeEntity.class)) {
                EmployeeEntity employeeEntity1 = (EmployeeEntity) oldValue;
                EmployeeEntity employeeEntity2 = (EmployeeEntity) newValue;
                oldVal = employeeEntity1.getFirstName() + " " + employeeEntity1.getLastName();
                newVal = employeeEntity2.getFirstName() + " " + employeeEntity2.getLastName();
            }


        ChangelogEntryEntity entry = new ChangelogEntryEntity(entity, field, oldVal.toString(), newVal.toString(),
                                                              ChangelogEntryEntity.ChangeType.UPDATED, LocalDate.now(),
                                                              user.getId());

        repository.save(entry);
        entity.addChangelogEntry(entry);
    }

    public static void notifyUpdateWithNull(AuditableEntity entity, String field, Object newValue) {

        Employee user = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Object newVal = newValue;

        if (newValue.getClass().equals(ClientEntity.class)) {
            ClientEntity clientEntity2 = (ClientEntity) newValue;
            newVal = clientEntity2.getName();
        } else if (newValue.getClass().equals(ProjectEntity.class)) {
            ProjectEntity projectEntity2 = (ProjectEntity) newValue;
            newVal = projectEntity2.getName();
        } else if (newValue.getClass().equals(SkillEntity.class)) {
            SkillEntity skillEntity2 = (SkillEntity) newValue;
            newVal = skillEntity2.getName();
        } else if (newValue.getClass().equals(ContactEntity.class)) {
            ContactEntity contactEntity2 = (ContactEntity) newValue;
            newVal = contactEntity2.getFirstName() + " " + contactEntity2.getLastName();
        } else if (newValue.getClass().equals(OrganisationUnitEntity.class)) {
            OrganisationUnitEntity organisationUnitEntity2 = (OrganisationUnitEntity) newValue;
            newVal = organisationUnitEntity2.getName();
        } else if (newValue.getClass().equals(EmployeeEntity.class)) {
            EmployeeEntity employeeEntity2 = (EmployeeEntity) newValue;
            newVal = employeeEntity2.getFirstName() + " " + employeeEntity2.getLastName();
        }

        if (newVal.toString() != "") {
            ChangelogEntryEntity entry = new ChangelogEntryEntity(entity, field, null, newVal.toString(),
                    ChangelogEntryEntity.ChangeType.UPDATED, LocalDate.now(),
                    user.getId());

            repository.save(entry);
            entity.addChangelogEntry(entry);
        }
    }

    /**
     * The given entity has been deleted.
     *
     * @param entity The entity that has been deleted.
     */
    public static void notifyDeletion(AuditableEntity entity) {
        Employee user = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ChangelogEntryEntity entry = new ChangelogEntryEntity(entity, ChangelogEntryEntity.ChangeType.DELETED, LocalDate.now(),
                                                              user.getId());
        repository.save(entry);
        entity.addChangelogEntry(entry);
    }

}
