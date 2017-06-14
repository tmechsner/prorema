package de.unibielefeld.techfak.tdpe.prorema.persistence;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.AuditableEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.AuditableEntityListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Singular;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Matthias on 4/16/16.
 */
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "client")
@PrimaryKeyJoinColumn(name = "client_id", referencedColumnName = "auditableEntity_id")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ClientEntity extends AuditableEntity {

    @NonNull
    @Column(name = "name")
    private String name;

    @NonNull
    @Lob
    @Column(name = "description")
    private String description;

    @NonNull
    @Column(name = "location")
    private String location;

    @Singular
    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private Set<ContactEntity> contactEntities;

    public ClientEntity() {
    }

}
