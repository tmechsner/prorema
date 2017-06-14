package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.domain.audit.Auditable;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ContactEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contact domain representing a contact.
 */
@Setter
@Getter
public class Contact extends Auditable {

    private String nameTitle;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    private String mail;
    private String tel;
    private String street;
    private String zip;
    private String city;
    private String country;
    private final List<Integer> projectIdList = new LinkedList<>();
    private Integer id;
    private Integer clientId;

    /**
     * @param nameTitle title as part of the name
     * @param firstName first name
     * @param lastName  last name
     * @param mail      email address
     * @param tel       telephone number
     * @param street    street as part of the address
     * @param zip       zip as part of the address
     * @param city      city as part of the address
     * @param country   country as part of the address
     */
    public Contact(String nameTitle, String firstName, String lastName, String mail, String tel, String street,
                   String zip, String city, String country, List<ChangelogEntryEntity> historyEntries) {
        this.nameTitle = nameTitle;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.tel = tel;
        this.street = street;
        this.zip = zip;
        this.city = city;
        this.country = country;

        List<ChangelogEntry> entries = new ArrayList<>();
        historyEntries.forEach(changelogEntryEntity -> {
            entries.add(new ChangelogEntry(changelogEntryEntity));
        });

        this.historyEntries = entries;
    }

    /**
     * @param entity persistent entity of this contact
     */
    public Contact(@NonNull ContactEntity entity) {



        this(entity.getNameTitle(), entity.getFirstName(), entity.getLastName(), entity.getEmail(), entity.getTel(),
             entity.getStreet(), entity.getZip(), entity.getCity(), entity.getCountry(), new LinkedList<ChangelogEntryEntity>(entity.getChangelogEntries()));
        this.id = entity.getId();
        clientId = entity.getClient().getId();
        List<ProjectEntity> projectEntities = entity.getProjects();
        if (projectEntities != null && !projectEntities.isEmpty()) {
            projectIdList.addAll(projectEntities.parallelStream()
                                                .filter(projectEntity -> projectEntity != null)
                                                .map(e -> e.getId())
                                                .collect(Collectors.toList()));
        }
    }


}
