package de.unibielefeld.techfak.tdpe.prorema.persistence;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.AuditableEntity;
import de.unibielefeld.techfak.tdpe.prorema.serializer.JsonDateDeserializer;
import de.unibielefeld.techfak.tdpe.prorema.serializer.JsonDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

/**
 * This Class represents the data of our projects in the database.
 * Created by Frederik on 18.04.2016.
 */
@Setter
@Getter
@AllArgsConstructor
@Entity
@Table(name = "project")
@PrimaryKeyJoinColumn(name = "project_id", referencedColumnName = "auditableEntity_id")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ProjectEntity extends AuditableEntity {

    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "street")
    private String street;

    @Column(name = "zip")
    private String zip;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "project_volume") //value in money
    private BigDecimal projectVolume;

    @Column(name = "conversion_probability") //probability in percent
    private Integer conversionProbability;

    @Column(name = "is_running")
    private Boolean running;

    @ManyToOne
    @JoinColumn(name = "organisationunit_id", referencedColumnName = "organisationunit_id", nullable = false)
    @Getter(onMethod = @__(@JsonIdentityReference))
    private OrganisationUnitEntity organisationUnit;

    @Column(name = "start_date")
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private LocalDate startDate;

    @Column(name = "end_date")
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private LocalDate endDate;

    @Column(name = "man_days")
    private Integer manDays;

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private EmployeeEntity projectManager;

    @ManyToOne
    @JoinColumn(name = "contact_id", referencedColumnName = "contact_id")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private ContactEntity contact;

    @OneToMany(fetch = FetchType.EAGER)
    @Getter(onMethod = @__(@JsonIgnore))
    private Set<NeedsSkillEntity> needSkills;


    //constructors
    public ProjectEntity() {
    }

    /**
     * @param name                  name
     * @param description           description
     * @param status                status
     * @param projectVolume         volume of this project
     * @param conversionProbability probability of conversion
     * @param running             if this project is running
     * @param manDays               man days required for this project
     * @param startDate             start date
     * @param endDate               end date
     */
    public ProjectEntity(String name, String description, String status, BigDecimal projectVolume,
                         Integer conversionProbability, Boolean running, Integer manDays, LocalDate startDate,
                         LocalDate endDate, String country, String city, String street, String zip) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.projectVolume = projectVolume;
        this.conversionProbability = conversionProbability;
        this.running = running;
        this.manDays = manDays;
        this.startDate = startDate;
        this.endDate = endDate;
        this.country = country;
        this.city = city;
        this.street = street;
        this.zip = zip;
    }

    public boolean getRunning () {
        if ((running) && (endDate != null)) {
            running = endDate.isAfter(LocalDate.now());
        }
        return running;
    }

    @JsonDeserialize(using = JsonDateDeserializer.class)
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public LocalDate getStartDate() {
        return this.startDate;
    }

    @JsonDeserialize(using = JsonDateDeserializer.class)
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public LocalDate getEndDate() {
        return this.endDate;
    }
}
