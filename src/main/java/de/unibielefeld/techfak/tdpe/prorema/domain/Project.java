package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.domain.audit.Auditable;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.persistence.*;
import de.unibielefeld.techfak.tdpe.prorema.persistence.NeedsSkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill.SkillLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Timo Mechsner on 27.04.16.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class Project extends Auditable implements FutureProject {
    private static final int HASH_FACTOR = 31;

    @NonNull
    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer menDays;

    private BigDecimal potentialProjectVolume;

    private Integer conversionProbability;

    @NonNull
    private Boolean running;

    private String street;

    private String zip;

    private String city;

    private String country;

    private Integer id;

    private Integer contactId;

    private Integer orgaUnitId;

    private Integer projectManagerId;

    private Status status;

    private String remainingTime;

    private final List<Tuple<Skill, SkillLevel>> skillList = new LinkedList<>();

    /**
     * Create a project from an entity of it's corresponding type.
     *
     * @param entity Entity to create a domain object from.
     */
    public Project(ProjectEntity entity) {
        this(entity.getName(),
             entity.getRunning());
        this.id = entity.getId();
        this.description = entity.getDescription();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.menDays = entity.getManDays();
        this.potentialProjectVolume = entity.getProjectVolume();
        this.conversionProbability = entity.getConversionProbability();
        this.country = entity.getCountry();
        this.city = entity.getCity();
        this.street = entity.getStreet();
        this.zip = entity.getZip();
        ContactEntity contactEntity = entity.getContact();
        if (contactEntity != null) {
            this.contactId = contactEntity.getId();
        }
        OrganisationUnitEntity organisationUnitEntity = entity.getOrganisationUnit();
        if (organisationUnitEntity != null) {
            this.orgaUnitId = organisationUnitEntity.getId();
        }
        EmployeeEntity projectManagerEntity = entity.getProjectManager();
        if (projectManagerEntity != null) {
            this.projectManagerId = projectManagerEntity.getId();
        }
        this.status = Status.getByName(entity.getStatus());

        Set<NeedsSkillEntity> needSkillSet = entity.getNeedSkills();
        if (needSkillSet != null) {
            skillList.addAll(needSkillSet.parallelStream()
                                         .filter(needsSkill -> needsSkill != null && needsSkill.getSkill() != null)
                                         .map(needsSkill ->
                                                      new Tuple<>(new Skill(needsSkill.getSkill()),
                                                                  Skill.SkillLevel.fromString(needsSkill.getLevel())))
                                         .collect(Collectors.toList()));
        }

        List<ChangelogEntryEntity> historyEntries = new LinkedList<>(entity.getChangelogEntries());
        List<ChangelogEntry> entries = new ArrayList<>();
        historyEntries.forEach(changelogEntryEntity -> {
            entries.add(new ChangelogEntry(changelogEntryEntity));
        });

        this.historyEntries = entries;
    }

    public void addSkill(Tuple<Skill, Skill.SkillLevel> newSkill) {
        skillList.add(newSkill);
    }

    public void setStatus(String status) {
        this.status = Status.getByName(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Project project = (Project) o;

        if (!id.equals(project.id)) {
            return false;
        }
        return name.equals(project.name);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = HASH_FACTOR * result + name.hashCode();
        result = HASH_FACTOR * result + (startDate != null ? startDate.hashCode() : 0);
        return result;
    }

    @Override
    public boolean isFutureProject(Status pos) {
        return pos.name.equals("Open");
    }

    public Employee getOwner() {
        return null;
    }

    public BigDecimal getWeightedProjectVolume() {
        if(potentialProjectVolume != null) {
            if (conversionProbability != null) {
                return potentialProjectVolume
                        .multiply(BigDecimal.valueOf(conversionProbability).divide(BigDecimal.valueOf(100.0)))
                        .setScale(2);
            } else {
                return potentialProjectVolume;
            }
        } else {
            return null;
        }
    }

    /**
     * An enum of all possible project states.
     */
    public enum Status {
        /**
         * Possible states of a project.
         */
        //FIXME: Translate to german
        OPEN("Open"), PROPOSAL("Proposal"), WON("Won"), LOST("Lost"), ONHOLD("On hold");

        /**
         * German representation of this state.
         */
        public final String name;

        Status(String name) {
            this.name = name;
        }

        /**
         * Get a status by it's name as string.
         *
         * @param name Name of a status
         * @return A Status enum entry or null if the given name did not match any status.
         */
        public static Status getByName(String name) {
            Optional<Status> status = Arrays.stream(Status.values())
                                            .filter(st -> st.name.equalsIgnoreCase(name))
                                            .findAny();
            return status.orElse(null);
        }
    }
}
