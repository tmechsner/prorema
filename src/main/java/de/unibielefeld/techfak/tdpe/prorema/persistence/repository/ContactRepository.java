package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.ContactEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * ContactRepository.
 */
public interface ContactRepository extends CrudRepository<ContactEntity, Integer> {
}
