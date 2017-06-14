package de.unibielefeld.techfak.tdpe.prorema.persistence.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.data.annotation.Transient;

import javax.persistence.*;
import java.util.*;

/**
 * Superclass that adds the capability of tracking changes.
 * Created by timo on 20.05.16.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "AUDITABLEENTITY")
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners(AuditableEntityListener.class)
public class AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="AUDITABLEENTITY_ID", unique = true, nullable = false)
    private Integer id;

    /**
     * All field values of the object when loaded.
     */
    @Transient
    @Getter(onMethod = @__({@JsonIgnore}))
    private transient Map<String, Object> loadedValues;

    /**
     * List of changelog entries.
     */
    @OneToMany(mappedBy = "auditableEntity", fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    @Getter(onMethod = @__({@JsonIgnore}))
    private Set<ChangelogEntryEntity> changelogEntries = new LinkedHashSet<>();

    /**
     * Add a changelog entry.
     * @param entry
     */
    public void addChangelogEntry(ChangelogEntryEntity entry) {
        changelogEntries.add(entry);
    }

}
