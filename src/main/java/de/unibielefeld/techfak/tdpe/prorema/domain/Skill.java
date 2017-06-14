package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.domain.audit.Auditable;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.HasSkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.NeedsSkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.SkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Skill domain representing a general skill.
 */
@Setter
@Getter
@RequiredArgsConstructor
public class Skill extends Auditable {

    private final Integer id;
    private String name;
    private String description;
    private final List<Integer> employeeIds = new LinkedList<>();
    private final List<Integer> projectIds = new LinkedList<>();

    /**
     * @param name String representation of this skill (lower case).
     */
    public Skill(String name) {
        id = null;
    }

    /**
     * @param entity persistence entity representing this domain.
     */
    public Skill(SkillEntity entity) {
        id = entity.getId();
        name = entity.getName();
        description = entity.getDescription();

        Set<HasSkillEntity> hasSkillSet = entity.getHasSkills();
        if (hasSkillSet != null) {
            employeeIds.addAll(hasSkillSet.parallelStream()
                                          .filter(hs -> hs != null)
                                          .map(HasSkillEntity::getEmployee)
                                          .filter(e -> e != null)
                                          .map(EmployeeEntity::getId)
                                          .collect(Collectors.toList()));
        }

        Set<NeedsSkillEntity> needsSkillSet = entity.getNeedsSkills();
        if (needsSkillSet != null) {
            projectIds.addAll(entity.getNeedsSkills().parallelStream()
                                    .filter(p -> p != null)
                                    .map(p -> p.getProject().getId())
                                    .collect(Collectors.toList()));
        }

        List<ChangelogEntryEntity> historyEntries = new LinkedList<>(entity.getChangelogEntries());
        List<ChangelogEntry> entries = new ArrayList<>();
        historyEntries.forEach(changelogEntryEntity -> {
            entries.add(new ChangelogEntry(changelogEntryEntity));
        });

        this.historyEntries = entries;
    }

    /**
     * 3 Skillevels, hardcoded.
     */
    public enum SkillLevel {

        /**
         * Basic knowledge.
         */
        BEGINNER("Anfänger", 0),
        /**
         * Medium amount of knowledge.
         */
        ADVANCED("Fortgeschritten", 1),
        /**
         * Like he was made for that job.
         */
        EXPERT("Experte", 2);

        /**
         * German string representation of skill.
         */
        private String nameDe;

        /**
         * Integer for comparison
         */
        private int intLevel;

        SkillLevel(String nameDe, int intLevel) {
            this.nameDe = nameDe;
            this.intLevel = intLevel;
        }

        public int getIntLevel() {
            return this.intLevel;
        }

        @Override public String toString() {
            return nameDe;
        }

        /**
         * Return position to a german string representation.
         *
         * @param nameDe german string
         * @return related position
         * @throws IllegalArgumentException if string is not known/assignable.
         */
        public static SkillLevel fromString(String nameDe) {
            for (SkillLevel element : SkillLevel.values()) {
                if (element.toString().equalsIgnoreCase(nameDe)) {
                    return element;
                }
            }
            throw new IllegalArgumentException("Skilllevel with name " + nameDe + " not known.");
        }

        /**
         * Checks whether this skill level is higher than the specified one.
         * @param other Skill level to compare to
         * @return Returns an int that describes the coverage. If the skill is covered the value is <= 0.
         */
        public int matchesWith(SkillLevel other) {
            return (this.intLevel - other.intLevel);
        }
    }
}
