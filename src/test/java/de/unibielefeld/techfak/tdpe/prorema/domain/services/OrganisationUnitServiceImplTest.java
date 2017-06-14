package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewOrganisationUnitCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.OrganisationUnitRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Benedikt Volkmer
 *         Created on 7/7/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrganisationUnitServiceImplTest {

    private final Random random = new Random();

    @Mock OrganisationUnitRepository repository;
    @Mock EmployeeRepository employeeRepository;

    private OrganisationUnitServiceImpl service;

    @Before
    public void setUp() throws Exception {
        service = new OrganisationUnitServiceImpl(repository, employeeRepository);
    }

    @Test
    public void init() throws Exception {
        // given
        final Integer id = random.nextInt();
        final String name = Double.toString(random.nextDouble());
        final String description = Double.toString(random.nextDouble());
        final Integer firstManagerId = random.nextInt();
        final EmployeeEntity firstManager = mock(EmployeeEntity.class);
        final EmployeeEntity secondManager = mock(EmployeeEntity.class);
        final Integer secondManagerId = random.nextInt();
        final OrganisationUnitEntity entity = mock(OrganisationUnitEntity.class);
        final List<Integer> employeeIds = random.ints().boxed().limit(100).collect(Collectors.toList());
        final List<EmployeeEntity> employees = employeeIds.stream().map(integer -> {
            EmployeeEntity mock = mock(EmployeeEntity.class);
            given(mock.getId()).willReturn(integer);
            return mock;
        }).collect(Collectors.toList());
        final List<Integer> projectIds = random.ints().boxed().limit(100).collect(Collectors.toList());
        final List<ProjectEntity> projects = projectIds.stream().map(integer -> {
            ProjectEntity mock = mock(ProjectEntity.class);
            given(mock.getId()).willReturn(integer);
            return mock;
        }).collect(Collectors.toList());
        given(entity.getId()).willReturn(id);
        given(entity.getEmployees()).willReturn(employees);
        given(entity.getName()).willReturn(name);
        given(entity.getDescription()).willReturn(description);
        given(entity.getFirstManager()).willReturn(firstManager);
        given(entity.getSecondManager()).willReturn(secondManager);
        given(entity.getProjects()).willReturn(projects);
        given(firstManager.getId()).willReturn(firstManagerId);
        given(secondManager.getId()).willReturn(secondManagerId);
        // when
        OrganisationUnit result = service.init(entity);
        // then
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", name)
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("firstManagerId", firstManagerId)
                .hasFieldOrPropertyWithValue("secondManagerId", secondManagerId);
        assertThat(result.getEmployeeIdList()).containsExactlyElementsOf(employeeIds);
        assertThat(result.getProjects()).containsExactlyElementsOf(projectIds);
    }

    @Test
    public void getAll() throws Exception {
        // given
        List<Integer> ids = random.ints().boxed().limit(100).collect(Collectors.toList());
        List<OrganisationUnitEntity> entities = createOUEntities(ids);
        given(repository.findAll()).willReturn(entities);
        // when
        List<OrganisationUnit> result = service.getAll();
        // then
        assertThat(result).extracting("id").containsExactlyElementsOf(ids);
    }

    @Test
    public void getAllExceptAdminUnit() throws Exception {
        // given
        List<Integer> ids = random.ints().boxed().limit(100).collect(Collectors.toList());
        ids.add(4000); // aka. AdminUnit
        List<OrganisationUnitEntity> entities = createOUEntities(ids);
        given(repository.findAll()).willReturn(entities);
        // when
        List<OrganisationUnit> result = service.getAllExceptAdminUnit();
        // then
        assertThat(result).extracting("id").containsExactlyElementsOf(
                                  ids.stream().filter(integer -> integer != 4000).collect(Collectors.toList()));
    }

    @Test
    public void createOrganisationUnit() throws Exception {
        // given
        NewOrganisationUnitCmd command = null;
        // when
        OrganisationUnit result = service.createOrganisationUnit(command);
        // then
        assertThat(result).as("null command is given").isNull();

        // given
        command = mock(NewOrganisationUnitCmd.class);
        Integer id = random.nextInt();
        String name = Double.toString(random.nextDouble());
        String description = Double.toString(random.nextDouble());
        Integer firstManagerId = random.nextInt();
        EmployeeEntity firstManager = mock(EmployeeEntity.class);
        Integer secondManagerId = random.nextInt();
        EmployeeEntity secondManager = mock(EmployeeEntity.class);
        given(employeeRepository.findOne(firstManagerId)).willReturn(firstManager);
        given(employeeRepository.findOne(secondManagerId)).willReturn(secondManager);
        given(firstManager.getId()).willReturn(firstManagerId);
        given(secondManager.getId()).willReturn(secondManagerId);
        given(command.getName()).willReturn(name);
        given(command.getDescription()).willReturn(description);
        given(command.getFirstManagerId()).willReturn(firstManagerId.toString());
        given(command.getSecondManagerId()).willReturn(secondManagerId.toString());
        given(repository.save(any(OrganisationUnitEntity.class))).willAnswer(invocation -> {
            OrganisationUnitEntity entity = (OrganisationUnitEntity) invocation.getArguments()[0];
            if (mockingDetails(entity).isMock()) {
                given(entity.getId()).willReturn(id);
            }
            entity.setId(id);
            return entity;
        });
        // when
        result = service.createOrganisationUnit(command);
        // then
        verify(repository).save(any(OrganisationUnitEntity.class));
        assertThat(result).as("a new ou without id is given")
                .hasFieldOrPropertyWithValue("name", name)
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("firstManagerId", firstManagerId)
                .hasFieldOrPropertyWithValue("secondManagerId", secondManagerId);

        // given
        OrganisationUnitEntity entity = mock(OrganisationUnitEntity.class);
        given(command.getId()).willReturn(id.toString());
        given(repository.exists(id)).willReturn(true);
        given(repository.findOne(id)).willReturn(entity);
        given(entity.getName()).willReturn(name);
        given(entity.getDescription()).willReturn(description);
        // when
        result = service.createOrganisationUnit(command);
        // then
        verify(repository).save(entity);
        verify(entity).setName(name);
        verify(entity).setDescription(description);
        verify(entity).setFirstManager(firstManager);
        verify(entity).setSecondManager(secondManager);
    }

    private List<OrganisationUnitEntity> createOUEntities(List<Integer> ids) {
        return ids.stream().map(integer -> {
            OrganisationUnitEntity mock = mock(OrganisationUnitEntity.class);
            given(mock.getId()).willReturn(integer);
            given(mock.getName()).willReturn(Double.toString(random.nextDouble()));
            given(mock.getDescription()).willReturn(Double.toString(random.nextDouble()));
            return mock;
        }).collect(Collectors.toList());

    }

}