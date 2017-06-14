package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Alex Schneider.
 */
public interface EmployeeProfileRepository extends CrudRepository<EmployeeProfileEntity, Integer>  {

    List<EmployeeProfileEntity> findById(int employeeId);
}
