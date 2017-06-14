package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Organisation unit repository.
 */
public interface OrganisationUnitRepository extends CrudRepository<OrganisationUnitEntity, Integer> {
}
