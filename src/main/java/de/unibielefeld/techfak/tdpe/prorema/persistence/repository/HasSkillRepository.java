package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.HasSkillEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Repo responsible for HasSkillEntity
 * Created by david on 28.04.2016.
 */
public interface HasSkillRepository extends CrudRepository<HasSkillEntity, Integer> {

    List<HasSkillEntity> findByEmployee_Id(int employeeId);

}
