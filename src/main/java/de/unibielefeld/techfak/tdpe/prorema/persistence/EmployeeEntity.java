
package de.unibielefeld.techfak.tdpe.prorema.persistence;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.AuditableEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.AuditableEntityListener;
import de.unibielefeld.techfak.tdpe.prorema.serializer.JsonDateDeserializer;
import de.unibielefeld.techfak.tdpe.prorema.serializer.JsonDateSerializer;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * EmployeeEntity.
 */
@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "EMPLOYEE")
@PrimaryKeyJoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "AUDITABLEENTITY_ID")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class EmployeeEntity extends AuditableEntity {

    /**
     * Mapping to SkillEntity.
     */

    @OneToMany(mappedBy = "employee", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private Set<HasSkillEntity> hasSkills;

    @NonNull
    @Column(name = "name_title")
    private String nameTitle;

    @NonNull
    @Column(name = "first_name")
    private String firstName;

    @NonNull
    @Column(name = "last_name")
    private String lastName;

    @NonNull
    @Column(name = "position")
    private String position;

    @NonNull
    @Column(name = "email", unique = true)
    private String email;

    @NonNull
    @Column(name = "tel")
    private String tel;

    @NonNull
    @Column(name = "street")
    private String street;

    @NonNull
    @Column(name = "zip")
    private String zip;

    @NonNull
    @Column(name = "city")
    private String city;

    @NonNull
    @Column(name = "country")
    private String country;

    @NonNull
    @Column(name = "workschedule")
    private Integer workschedule;

    @NonNull
    @Column(name = "workentry")
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private LocalDate workEntry;

    @NonNull
    @Column(name = "workexit")
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private LocalDate workExit;

    @NonNull
    @Column(name = "username", unique = true)
    private String username;

    @NonNull
    @Column(name = "password")
    private String password;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "organisationunit_id", referencedColumnName = "organisationunit_id")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private OrganisationUnitEntity organisationUnit;

    @OneToMany(mappedBy = "firstManager", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private List<OrganisationUnitEntity> firstManagerOrganisationIds;

    @OneToMany(mappedBy = "secondManager", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private List<OrganisationUnitEntity> secondManagerOrganisationIds;

    @OneToMany(mappedBy = "projectManager", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private Set<ProjectEntity> managerProjectIds;

    @Singular
    @OneToMany(mappedBy = "employee", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private Set<EmployeeProfileEntity> employeeProfiles;

    public EmployeeEntity() {
    }


    public EmployeeEntity(String nameTitle, String firstName, String lastName, String position, String email,
                          String tel, LocalDate workEntry, LocalDate workExit, String street, String zip, String city,
                          String country, Integer workschedule, String username, String password,
                          OrganisationUnitEntity organisationUnit) {
        this();
        this.nameTitle = nameTitle;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.email = email;
        this.tel = tel;
        this.workEntry = workEntry;
        this.workExit = workExit;
        this.street = street;
        this.zip = zip;
        this.city = city;
        this.country = country;
        this.workschedule = workschedule;
        this.username = username;
        this.password = password;
        this.organisationUnit = organisationUnit;
    }


    public Integer getWorkschedule() {
        if(this.workschedule != null){
            return workschedule;
        } else
            return this.workschedule = 0;
    }

    public String getUsername(){
        if(this.username != null){
            return username;
        } else
            return this.username = firstName.concat(this.getId().toString());
    }

    public String getPassword(){
        if(this.password != null){
            return password;
        } else
            return this.password = lastName.concat(this.getId().toString());
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public LocalDate getWorkEntry(){
        return this.workEntry;
    }

    @JsonDeserialize(using = JsonDateDeserializer.class)
    public void setWorkEntry(LocalDate workEntry){
        this.workEntry = workEntry;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public LocalDate getWorkExit(){
        return this.workExit;
    }

    @JsonDeserialize(using = JsonDateDeserializer.class)
    public void setWorkExit(LocalDate workExit){
        this.workExit = workExit;
    }
}
