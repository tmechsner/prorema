package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.SkillEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository handling SkillEntities.
 */
public interface SkillRepository extends CrudRepository<SkillEntity, Integer> {
}
