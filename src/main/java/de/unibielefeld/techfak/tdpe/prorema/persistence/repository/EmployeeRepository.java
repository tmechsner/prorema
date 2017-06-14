package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Matthias on 4/22/16.
 */
@Primary
public interface EmployeeRepository extends CrudRepository<EmployeeEntity, Integer> {

    List<EmployeeEntity> findByPosition(String position);

    List<EmployeeEntity> findByOrganisationUnit_Id(Integer id);

}
