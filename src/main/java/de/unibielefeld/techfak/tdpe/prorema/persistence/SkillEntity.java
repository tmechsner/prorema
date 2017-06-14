package de.unibielefeld.techfak.tdpe.prorema.persistence;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.AuditableEntity;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

/**
 * SkillEntity.
 */
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity(name = "SKILL")
@PrimaryKeyJoinColumn(name = "SKILL_ID", referencedColumnName = "auditableEntity_id")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class SkillEntity extends AuditableEntity {

    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "skill", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private Set<HasSkillEntity> hasSkills;

    @OneToMany(mappedBy = "skill", fetch = FetchType.EAGER)
    @Getter(onMethod = @__({@JsonIgnore}))
    private Set<NeedsSkillEntity> needsSkills;
}

