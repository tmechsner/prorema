package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by timo on 30.06.16.
 */
public interface ChangelogEntryRepository extends CrudRepository<ChangelogEntryEntity, Integer> {
}
