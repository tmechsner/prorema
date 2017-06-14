package de.unibielefeld.techfak.tdpe.prorema.Mocks;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewEmployeeCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *  Created by Matthias on 4/22/16.
 */
public class EmployeeRepositoryMock implements EmployeeRepository {
    ArrayList<EmployeeEntity> emList = new ArrayList<EmployeeEntity>();

    public EmployeeRepositoryMock()  {
    }

    public void addEmployee(EmployeeEntity employee) {
        emList.add(employee);
    }

    @Override
    public List<EmployeeEntity> findByPosition(String position) {
        return null;
    }

    @Override
    public List<EmployeeEntity> findByOrganisationUnit_Id(Integer id) {
        return null;
    }

    @Override
    public <S extends EmployeeEntity> S save(S s) {
        return null;
    }

    @Override
    public <S extends EmployeeEntity> Iterable<S> save(Iterable<S> iterable) {
        return null;
    }

    @Override
    public EmployeeEntity findOne(Integer integer) {
        return null;
    }

    @Override
    public boolean exists(Integer integer) {
        return false;
    }

    @Override
    public Iterable<EmployeeEntity> findAll() {
        return emList;
    }

    @Override
    public Iterable<EmployeeEntity> findAll(Iterable<Integer> iterable) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(Integer integer) {

    }

    @Override
    public void delete(EmployeeEntity employeeEntity) {

    }

    @Override
    public void delete(Iterable<? extends EmployeeEntity> iterable) {

    }

    @Override
    public void deleteAll() {

    }
}
