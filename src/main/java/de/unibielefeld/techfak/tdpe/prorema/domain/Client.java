package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.domain.audit.Auditable;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ClientEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ContactEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Client domain representing an simple Client.
 */
@Setter
@Getter
@RequiredArgsConstructor
public class Client extends Auditable {

    private final List<Integer> contactIdList = new LinkedList<>();

    private Integer id;

    @NonNull
    private String name;

    @NonNull
    private String description;

    @NonNull
    private String location;


    /**
     * @param entity persistent entity representing this domain.
     */
    public Client(@NonNull ClientEntity entity) {
        this(entity.getName(), entity.getDescription(), entity.getLocation());
        this.id = entity.getId();

        List<ChangelogEntryEntity> historyEntries = new LinkedList<>(entity.getChangelogEntries());
        List<ChangelogEntry> entries = new ArrayList<>();
        historyEntries.forEach(changelogEntryEntity -> {
            entries.add(new ChangelogEntry(changelogEntryEntity));
        });

        this.historyEntries = entries;

        Set<ContactEntity> contactEntities = entity.getContactEntities();
        if (contactEntities != null && !contactEntities.isEmpty()) {
            contactEntities.parallelStream()
                           .filter(e -> e != null)
                           .map(ContactEntity::getId)
                           .forEach(contactIdList::add);
        }
    }

}
