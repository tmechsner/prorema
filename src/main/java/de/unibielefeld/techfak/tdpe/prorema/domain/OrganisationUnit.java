package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.domain.audit.Auditable;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Domain representing a organisation unit.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class OrganisationUnit extends Auditable {

    private Integer id;

    private List<Integer> employeeIdList = new LinkedList<>();

    @NonNull
    private String name;

    @NonNull
    private String description;

    private Integer firstManagerId;

    private Integer secondManagerId;

    private final List<Integer> projects = new LinkedList<>();


    /**
     * @param entity Entity of related organisation unit.
     */
    public OrganisationUnit(@NonNull OrganisationUnitEntity entity) {
        this(entity.getName(), entity.getDescription());
        this.id = entity.getId();
        if (entity.getFirstManager() != null) {
            this.firstManagerId = entity.getFirstManager().getId();
        }
        if (entity.getSecondManager() != null) {
            this.secondManagerId = entity.getSecondManager().getId();
        }
        List<ProjectEntity> projectEntities = entity.getProjects();
        if (projectEntities != null) {
            projects.addAll(projectEntities.parallelStream()
                                           .filter(projectEntity -> projectEntity != null)
                                           .map(ProjectEntity::getId)
                                           .collect(Collectors.toList()));
        }
        List<EmployeeEntity> employeeEntities = entity.getEmployees();
        if (employeeEntities != null) {
            employeeIdList.addAll(employeeEntities.parallelStream()
                                                  .filter(entity1 -> entity1 != null)
                                                  .map(EmployeeEntity::getId)
                                                  .collect(Collectors.toList()));
        }

        List<ChangelogEntryEntity> historyEntries = new LinkedList<>(entity.getChangelogEntries());
        List<ChangelogEntry> entries = new ArrayList<>();
        historyEntries.forEach(changelogEntryEntity -> {
            entries.add(new ChangelogEntry(changelogEntryEntity));
        });

        this.historyEntries = entries;
    }

}
