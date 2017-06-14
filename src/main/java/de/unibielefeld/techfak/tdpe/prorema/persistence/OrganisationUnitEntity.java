package de.unibielefeld.techfak.tdpe.prorema.persistence;

import com.fasterxml.jackson.annotation.*;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.AuditableEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Brian on 17.04.2016.
 */
@Setter
@Getter
@RequiredArgsConstructor
@Entity(name = "ORGANISATIONUNIT")
@PrimaryKeyJoinColumn(name = "ORGANISATIONUNIT_ID", referencedColumnName = "auditableEntity_id")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class OrganisationUnitEntity extends AuditableEntity {

    @NonNull
    @Column(name = "name")
    private String name;

    @NonNull
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "first_manager", referencedColumnName = "EMPLOYEE_ID")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private EmployeeEntity firstManager;

    @ManyToOne
    @JoinColumn(name = "second_manager", referencedColumnName = "EMPLOYEE_ID")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private EmployeeEntity secondManager;

    @OneToMany(mappedBy = "organisationUnit", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private List<EmployeeEntity> employees;

    @OneToMany(mappedBy = "organisationUnit", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private List<ProjectEntity> projects;

    public OrganisationUnitEntity() {
    }

    /*
    public void setId(Integer id) {
        this.id = id;
    }
    */
}
