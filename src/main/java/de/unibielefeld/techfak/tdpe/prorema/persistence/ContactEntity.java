package de.unibielefeld.techfak.tdpe.prorema.persistence;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.AuditableEntity;
import lombok.*;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Matthias on 4/16/16.
 */

@Setter
@Getter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "contact")
@PrimaryKeyJoinColumn(name = "contact_id", referencedColumnName = "auditableEntity_id")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ContactEntity extends AuditableEntity {

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
    @Column(name = "tel")
    private String tel;

    @NonNull
    @Column(name = "email")
    private String email;

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

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private ClientEntity client;

    @OneToMany(mappedBy = "contact", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private List<ProjectEntity> projects;

}
