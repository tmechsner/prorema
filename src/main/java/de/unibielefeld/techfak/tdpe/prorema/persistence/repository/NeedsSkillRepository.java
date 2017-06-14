package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.NeedsSkillEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Brian Baumbach on 12.05.2016.
 */
public interface NeedsSkillRepository extends CrudRepository<NeedsSkillEntity, Integer> {

    List<NeedsSkillEntity> findByProject_Id(int projectId);
}
