package de.unibielefeld.techfak.tdpe.prorema.domain.audit;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import lombok.*;

import java.time.LocalDate;

/**
 * Changelog entry.
 * Preserves information about who changed which field from which value to which value at what time.
 * History Entries wont be updated (no need to do this) or created manually (created automatically before database update of an audited entity).
 *
 * Created by timo on 20.05.16.
 */
@Getter
@Setter
@AllArgsConstructor
public class ChangelogEntry {

    /**
     * Unique ID of this changelog entry.
     */
    private Integer changelogEntryId;

    /**
     * Name of the field that has been updated.
     */
    private String fieldName;

    /**
     * Value of the field before the update.
     * Can be empty if type is CREATED or DELETED.
     */
    private String oldValue;

    /**
     * Value of the field after the update.
     * Can be empty if type is CREATED or DELETED.
     */
    private String newValue;

    /**
     * Does this Object describe a CREATED, UPDATED or DELETED event?
     */
    @NonNull
    private ChangeType changeType;

    /**
     * When did the update/creation/deletion take place?
     */
    @NonNull
    private LocalDate timestamp;

    /**
     * The user that did the update/creation/deletion.
     */
    @NonNull
    private Integer userId;

    /**
     * Enum to represent different types of data change events.
     */
    public enum ChangeType {
        CREATED, UPDATED, DELETED;

        public static ChangeType fromEntity(ChangelogEntryEntity.ChangeType changeType) {
            switch (changeType) {
                case CREATED:
                    return CREATED;
                case DELETED:
                    return DELETED;
                case UPDATED:
                default:
                    return UPDATED;
            }
        }


    }

    public ChangelogEntry (ChangelogEntryEntity changelogEntryEntity) {
        this.changelogEntryId = changelogEntryEntity.getChangelogEntryId();
        this.fieldName = changelogEntryEntity.getFieldName();
        this.oldValue = changelogEntryEntity.getOldValue();
        this.newValue = changelogEntryEntity.getNewValue();
        this.changeType = ChangeType.fromEntity(changelogEntryEntity.getChangeType());
        this.timestamp = changelogEntryEntity.getTimestamp();
        this.userId = changelogEntryEntity.getUserId();
    }

}
