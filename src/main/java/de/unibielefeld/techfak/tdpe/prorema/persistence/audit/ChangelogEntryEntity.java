package de.unibielefeld.techfak.tdpe.prorema.persistence.audit;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import javax.persistence.Entity;
import java.time.LocalDate;

/**
 * Entity changelog entry.
 * Preserves information about who changed which field from which value to which value at what time.
 *
 * Created by timo on 20.05.16.
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class ChangelogEntryEntity {

    @NonNull
    @ManyToOne
    @JoinColumn(name = "AUDITABLEENTITY_ID", referencedColumnName = "AUDITABLEENTITY_ID")
    private AuditableEntity auditableEntity;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "changelogEntry_id", unique = true, nullable = false)
    private Integer changelogEntryId;

    @Column
    private String fieldName;

    @Column
    private String oldValue;

    @Column
    private String newValue;

    @NonNull
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ChangeType changeType;

    @NonNull
    @Column(nullable = false)
    private LocalDate timestamp;

    @NonNull
    @Column(nullable = false)
    private Integer userId;

    /**
     * Enum to represent different types of data change events.
     */
    public enum ChangeType {
        CREATED, UPDATED, DELETED;
    }

    public ChangelogEntryEntity (AuditableEntity auditableEntity, String fieldName, String oldValue, String newValue,
                                 ChangeType changeType, LocalDate timestamp, Integer userId) {
        this.auditableEntity = auditableEntity;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = changeType;
        this.timestamp = timestamp;
        this.userId = userId;
    }

}
