package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewWorksOnCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOnDetails;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.WorksOnEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ProjectRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.WorksOnRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.hamcrest.Matchers;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Benedikt Volkmer
 *         Created on 7/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class WorksOnServiceImplTest {

    private Random random = new Random();

    @Mock private WorksOnRepository repository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ProjectRepository projectRepository;

    private WorksOnServiceImpl service;

    @Before
    public void setUp() throws Exception {
        service = new WorksOnServiceImpl(repository, employeeRepository, projectRepository);
    }

    @Test
    public void getByDateAndEmployeeAndGetByStatusAndGetByEmployeeAndByProject() throws Exception {
        // given
        Integer employeeId = random.nextInt();
        LocalDate start = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate end = LocalDate.now().with(DayOfWeek.SUNDAY);
        WorksOn.WorkStatus status = WorksOn.WorkStatus.values()[random.nextInt(WorksOn.WorkStatus.values().length)];
        List<Integer> ids = random.ints().boxed().limit(100).collect(Collectors.toList());
        List<WorksOnEntity> worksOnEntities = generatedMockedEntityList(ids);
        given(repository.findByEmployee_IdAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(anyInt(),
                                                                                                  any(LocalDate.class),
                                                                                                  any(LocalDate.class)))
                .willReturn(worksOnEntities);
        given(repository.findByStatus(anyString())).willReturn(worksOnEntities);
        given(repository.findByEmployee_Id(anyInt())).willReturn(worksOnEntities);
        given(repository.findByProject_Id(anyInt())).willReturn(worksOnEntities);
        // when
        List<WorksOn> byDateAndEmployee = service.getByDateAndEmployee(start, end, employeeId);
        List<WorksOn> byStatus = service.getByStatus(status.toString());
        List<WorksOn> byEmployee = service.getWorksOnByEmployee(employeeId);
        List<WorksOn> byProject = service.getWorksOnByProject(employeeId);
        // then
        verify(repository)
                .findByEmployee_IdAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(employeeId, start, end);
        verify(repository).findByStatus(status.toString());
        verify(repository).findByEmployee_Id(employeeId);
        verify(repository).findByProject_Id(employeeId);
        assertThat(byDateAndEmployee).extracting("id").containsOnlyElementsOf(ids);
        assertThat(byStatus).extracting("id").containsOnlyElementsOf(ids);
        assertThat(byEmployee).extracting("id").containsOnlyElementsOf(ids);
        assertThat(byProject).extracting("id").containsOnlyElementsOf(ids);
    }

    @Test
    public void getByStatus() throws Exception {
        // given
        WorksOn.WorkStatus status = WorksOn.WorkStatus.values()[random.nextInt(WorksOn.WorkStatus.values().length)];
        List<Integer> ids = random.ints().boxed().limit(100).collect(Collectors.toList());
        List<WorksOnEntity> worksOnEntities = generatedMockedEntityList(ids);
        // when
        // then
    }

    @Test
    public void init() throws Exception {
        // given
        final Integer id = random.nextInt();
        final WorksOnEntity entity = mock(WorksOnEntity.class);
        final EmployeeEntity employeeEntity = mock(EmployeeEntity.class);
        final ProjectEntity projectEntity = mock(ProjectEntity.class);
        final WorksOn.WorkStatus status = WorksOn.WorkStatus.values()[random.nextInt(WorksOn.WorkStatus.values().length)];
        final Integer workload = random.nextInt(50);
        Integer offset = random.nextInt();
        final LocalDate start = LocalDate.now().plusDays(offset);
        final LocalDate end = LocalDate.now().plusDays(offset).plusDays(random.nextInt(1000));
        final Integer employeeId = random.nextInt();
        final Integer projectId = random.nextInt();
        given(entity.getId()).willReturn(id);
        given(entity.getEmployee()).willReturn(employeeEntity);
        given(entity.getProject()).willReturn(projectEntity);
        given(entity.getStatus()).willReturn(status.toString());
        given(entity.getWorkload()).willReturn(workload);
        given(entity.getStartDate()).willReturn(start);
        given(entity.getEndDate()).willReturn(end);
        given(employeeEntity.getId()).willReturn(employeeId);
        given(projectEntity.getId()).willReturn(projectId);
        // when
        WorksOn result = service.init(entity);
        // then
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("projectId", projectId)
                .hasFieldOrPropertyWithValue("employeeId", employeeId)
                .hasFieldOrPropertyWithValue("workload", workload)
                .hasFieldOrPropertyWithValue("status", status)
                .hasFieldOrPropertyWithValue("startDate", start)
                .hasFieldOrPropertyWithValue("endDate", end);
    }

    @Test
    public void createWorksOn() throws Exception {
        // given
        NewWorksOnCmd command = null;
        // when
        List<WorksOn> result = service.createWorksOn(command);
        // then
        assertThat(result).as("command is null").isNull();

        // given
        command = mock(NewWorksOnCmd.class);
        final List<Integer> employeeIds = random.ints().boxed().limit(10).collect(Collectors.toList());
        final Integer projectId = random.nextInt();
        List<WorksOnDetails> worksOnDetailsList = new ArrayList<>();
        List<Integer> ids = random.ints().boxed().limit(10).collect(Collectors.toList());
        for (Integer subId : ids) {
            WorksOnDetails mock = mock(WorksOnDetails.class);
            final WorksOn.WorkStatus status = WorksOn.WorkStatus.values()[random.nextInt(WorksOn.WorkStatus.values().length)];
            final Integer workload = random.nextInt(50);
            Integer offset = random.nextInt();
            final LocalDate start = LocalDate.now().plusDays(offset);
            final LocalDate end = LocalDate.now().plusDays(offset).plusDays(random.nextInt(1000));
            given(mock.getId()).willReturn(subId);
            given(mock.getWorkload()).willReturn(workload);
            given(mock.getStartDate()).willReturn(start.toString());
            given(mock.getEndDate()).willReturn(end.toString());
            given(mock.getStatus()).willReturn(status.toString());
            worksOnDetailsList.add(mock);
        }
        given(command.getProjectId()).willReturn(projectId);
        given(command.getEmployeeIds()).willReturn(employeeIds);
        given(command.getWorkDetails()).willReturn(worksOnDetailsList);
        // when
        result = service.createWorksOn(command);
        // then
        assertThat(result).as("repository returns zero for projects and employee").isEmpty();

        // given
        ProjectEntity projectEntity = mock(ProjectEntity.class);
        given(projectRepository.findOne(projectId)).willReturn(projectEntity);
        LinkedList<EmployeeEntity> employeeEntities = new LinkedList<>(employeeIds.stream().map(subId -> mock(EmployeeEntity.class)).collect(
                Collectors.toList()));
        ListIterator<EmployeeEntity> employeeEntityIterator = employeeEntities.listIterator();
        given(employeeRepository.findOne(anyInt())).willAnswer(invocation -> {
            Integer subId = (Integer) invocation.getArguments()[0];
            if (employeeEntityIterator.hasPrevious()) {
                employeeEntityIterator.add(employeeEntityIterator.previous());
            }
            EmployeeEntity mock = employeeEntityIterator.next();
            given(mock.getId()).willReturn(subId);
            return mock;
        });
        WorksOnEntity entity = mock(WorksOnEntity.class);
        final EmployeeEntity employeeEntity = mock(EmployeeEntity.class);
        final WorksOn.WorkStatus status = WorksOn.WorkStatus.values()[random.nextInt(WorksOn.WorkStatus.values().length)];
        final Integer workload = random.nextInt(50);
        Integer offset = random.nextInt();
        final LocalDate start = LocalDate.now().plusDays(offset);
        final LocalDate end = LocalDate.now().plusDays(offset).plusDays(random.nextInt(1000));
        final Integer employeeId = random.nextInt();
        given(entity.getEmployee()).willReturn(employeeEntity);
        given(entity.getProject()).willReturn(projectEntity);
        given(entity.getStatus()).willReturn(status.toString());
        given(entity.getWorkload()).willReturn(workload);
        given(entity.getStartDate()).willReturn(start);
        given(entity.getEndDate()).willReturn(end);
        given(employeeEntity.getId()).willReturn(employeeId);
        given(projectEntity.getId()).willReturn(projectId);
        given(repository.save(any(WorksOnEntity.class))).willAnswer(invocation -> {
            WorksOnEntity currEntity = (WorksOnEntity) invocation.getArguments()[0];
            if (mockingDetails(currEntity).isMock()) {
                given(currEntity.getId()).willReturn(random.nextInt());
            } else {
                currEntity.setId(random.nextInt());
            }
            return currEntity;
        });
        // when
        result = service.createWorksOn(command);
        // then
        assertThat(result).as("worksOns are generated but do not exist").hasSize(100);
        verify(repository, times(100)).save(any(WorksOnEntity.class));

        // given
        Integer id = random.nextInt();
        given(command.getId()).willReturn(id);
        given(repository.findOne(id)).willReturn(entity);
        given(repository.exists(id)).willReturn(true);
        // when
        result = service.createWorksOn(command);
        // then
        assertThat(result).as("worksOns are generated but exists").hasSize(55);
        verify(repository, times(155)).save(any(WorksOnEntity.class)); // 55 plus the previous 100
        verify(entity, times(10)).setProject(projectEntity);
        verify(entity, times(10)).setEmployee(any(EmployeeEntity.class));
        verify(entity, times(10)).setStatus(anyString());
        verify(entity, times(10)).setWorkload(anyInt());
        verify(entity, times(10)).setStartDate(any(LocalDate.class));
        verify(entity, times(10)).setEndDate(any(LocalDate.class));
    }

    private List<WorksOnEntity> generatedMockedEntityList(List<Integer> ids) {
        List<WorksOnEntity> worksOnEntities = new ArrayList<>();
        for (Integer id : ids) {
            WorksOnEntity entity = mock(WorksOnEntity.class);
            ProjectEntity projectEntity = mock(ProjectEntity.class);
            EmployeeEntity employeeEntity = mock(EmployeeEntity.class);
            given(entity.getId()).willReturn(id);
            given(entity.getProject()).willReturn(projectEntity);
            given(entity.getStatus()).willReturn(WorksOn.WorkStatus.values()[random.nextInt(WorksOn.WorkStatus.values().length)].toString());
            given(entity.getEmployee()).willReturn(employeeEntity);
            Integer dateOffset = random.nextInt();
            given(entity.getStartDate()).willReturn(LocalDate.now().plusDays(dateOffset));
            given(entity.getEndDate()).willReturn(LocalDate.now().plusDays(dateOffset + random.nextInt(1000)));
            given(entity.getWorkload()).willReturn(random.nextInt());
            given(projectEntity.getId()).willReturn(random.nextInt());
            given(employeeEntity.getId()).willReturn(random.nextInt());
            worksOnEntities.add(entity);
        }
        return worksOnEntities;
    }

}