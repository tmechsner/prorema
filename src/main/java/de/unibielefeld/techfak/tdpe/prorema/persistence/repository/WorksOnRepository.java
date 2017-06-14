package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.WorksOnEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by David on 12.05.2016.
 */
public interface WorksOnRepository extends CrudRepository<WorksOnEntity, Integer> {

    //@Query(value = "select * from WORKSON where EMPLOYEE_ID = 1", nativeQuery = true)
    //@Query("select u from #{#entityName} where u.project.projedtId = 1")
    List<WorksOnEntity> findByProject_Id(int projectId);

    List<WorksOnEntity> findByEmployee_IdAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(int employeeId, LocalDate start, LocalDate end);

    List<WorksOnEntity> findByEmployee_Id(int employeeId);

    List<WorksOnEntity> findByStatus(String status);

}
