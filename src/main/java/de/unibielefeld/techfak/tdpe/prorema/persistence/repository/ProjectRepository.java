package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Matthias on 4/22/16.
 */
public interface ProjectRepository extends CrudRepository<ProjectEntity, Integer> {

    List<ProjectEntity> findByOrganisationUnit_Id(Integer id);

    List<ProjectEntity> findByRunning(Boolean running);

}
