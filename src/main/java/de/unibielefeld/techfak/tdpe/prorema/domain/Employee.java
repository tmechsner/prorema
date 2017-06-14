package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.domain.audit.Auditable;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.HasSkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill.SkillLevel;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Employee domain representing an abstract employee Created by Benedikt Volkmer on 4/15/16.
 * <p> The Part implementing  {@link UserDetails}, and {@link CredentialsContainer} are derived
 * from {@link org.springframework.security.core.userdetails.User}</p>
 */
@Setter
@Getter
public class Employee extends Auditable implements UserDetails, CredentialsContainer {

    //Domain specific fields
    private String nameTitle;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    private String mail;
    private String tel;
    private String street;
    private String zip;
    private String city;
    private String country;
    private Integer id;
    private Position position;
    private List<Integer> employeeProfiles = new LinkedList<>();
    private List<Integer> projectManagers = new LinkedList<>();
    private int workSchedule;
    private LocalDate workEntry;
    private LocalDate workExit;
    private final List<Tuple<Skill, SkillLevel>> skillList = new LinkedList<>();
    private Integer organisationUnitId;
    //UserDetails specific fields
    private String password;
    private String username;
    private final Set<GrantedAuthority> authorities;
    private List<List<InformationPacker>> informationPacker;

    /**
     * @param nameTitle    Title as part of the name
     * @param firstName    First Name of this employee
     * @param lastName     Last Name of this employee
     * @param mail         email of this employee
     * @param tel          telephone number as a string, to represent special forms, i.e. country codes
     * @param street       Street as part of the address
     * @param zip          ZIP as part of the address
     * @param city         City as part of the address
     * @param country      Country as part of the address
     * @param position     position of this entity
     * @param workSchedule workSchedule of this entity
     * @param workEntry    workEntry date of this entity
     * @param workExit     workExit date of this entity
     * @param username     username of this entity
     * @param password     password of this entity
     */
    public Employee(String nameTitle, String firstName, String lastName, String mail, String tel, String street,
                    String zip, String city, String country, Position position,
                    int workSchedule, LocalDate workEntry, LocalDate workExit, String username, String password) {
        this.nameTitle = nameTitle;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.tel = tel;
        this.street = street;
        this.zip = zip;
        this.city = city;
        this.country = country;
        this.position = position;
        this.workSchedule = workSchedule;
        this.workEntry = workEntry;
        this.workExit = workExit;

        //UserDetails implementatio
        this.username = username;
        this.password = password;
        this.authorities = Collections.unmodifiableSet(sortAuthorities(
                Arrays.asList(new SimpleGrantedAuthority("USER"))));

    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return authorities;
    }


    /**
     * @param entity persistent entity representing this domain.
     */
    public Employee(@NonNull EmployeeEntity entity) {
        this(entity.getNameTitle(), entity.getFirstName(), entity.getLastName(), entity.getEmail(),
                entity.getTel(), entity.getStreet(), entity.getZip(), entity.getCity(), entity.getCountry(),
                Position.fromString(entity.getPosition()),
                entity.getWorkschedule(), entity.getWorkEntry(), entity.getWorkExit(),
                entity.getUsername(), entity.getPassword());
        if (entity.getOrganisationUnit() != null) {
            this.organisationUnitId = entity.getOrganisationUnit().getId();
        }

        this.id = entity.getId();

        Set<ProjectEntity> managerProjects = entity.getManagerProjectIds();
        if (managerProjects != null && !managerProjects.isEmpty()){
            managerProjects.parallelStream()
                    .filter(e -> e != null)
                    .map(ProjectEntity::getId)
                    .forEach(projectManagers::add);
        }

        Set<EmployeeProfileEntity> employeeProfileEntities = entity.getEmployeeProfiles();
        if (employeeProfileEntities != null && !employeeProfileEntities.isEmpty()) {
            employeeProfileEntities.parallelStream()
                    .filter(e -> e != null)
                    .map(EmployeeProfileEntity::getId)
                    .forEach(employeeProfiles::add);
        }

        Set<HasSkillEntity> hasSkillSet = entity.getHasSkills();
        if (hasSkillSet != null) {
            skillList.addAll(entity.getHasSkills().parallelStream()
                    .filter(p -> p != null)
                    .map(p -> new Tuple<>(new Skill(p.getSkill()), SkillLevel.fromString(p.getLevel())))
                    .collect(Collectors.toList()));
        }

        List<ChangelogEntryEntity> historyEntries = new LinkedList<>(entity.getChangelogEntries());
        List<ChangelogEntry> entries = new ArrayList<>();
        historyEntries.forEach(changelogEntryEntity -> {
            entries.add(new ChangelogEntry(changelogEntryEntity));
        });

        this.historyEntries = entries;

    }

    private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends
            GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
        // Ensure array iteration order is predictable (as per
        // UserDetails.getAuthorities() contract and SEC-717)
        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(new AuthorityComparator());

        for (GrantedAuthority grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
            sortedAuthorities.add(grantedAuthority);
        }

        return sortedAuthorities;
    }

    public void addSkill(Tuple<Skill, SkillLevel> newSkillTuple) {
        skillList.add(newSkillTuple);
    }

    public void addSkills(List<Tuple<Skill, SkillLevel>> newSkills) {
        skillList.addAll(newSkills);
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Employee && username.equals(((Employee) obj).getUsername());
    }

    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {
        private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

        AuthorityComparator() {
            super();
        }

        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
            // Neither should ever be null as each entry is checked before adding it to
            // the set.
            // If the authority is null, it is a custom authority and should precede
            // others.
            if (g2.getAuthority() == null) {
                return -1;
            }

            if (g1.getAuthority() == null) {
                return 1;
            }

            return g1.getAuthority().compareTo(g2.getAuthority());
        }
    }

    /**
     * Hardcoded Position an Employee might have.
     */
    public enum Position {
        /**
         * Possible positions of managers.
         */
        PARTNER("Partner",2500), SENIOR_MANAGER("Senior Manager",10000), MANAGER("Manager",5000),
        /**
         * Possible positions of staff.
         */
        SENIOR_CONSULTANT("Senior Berater",1000), CONSULTANT("Berater",500),
        /**
         * Administrator.
         */
        ADMINISTRATOR("Administrator",1000000);

        /**
         * German string representation of position.
         */
        private String nameDe;
        private int priority;

        Position(String nameDe, int priority) {
            this.nameDe = nameDe;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return nameDe;
        }

        /**
         * Return position to a german string representation.
         *
         * @param nameDe german string
         * @return related position
         * @throws IllegalArgumentException if string is not known/assignable.
         */
        public static Position fromString(String nameDe) {
            for (Position element : Position.values()) {
                if (element.toString().equalsIgnoreCase(nameDe)) {
                    return element;
                }
            }
            throw new IllegalArgumentException("Position with name " + nameDe + " not known.");
        }

        public int getPriority() {
            return priority;
        }
    }
}
