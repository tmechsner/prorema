package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht.ScheduleElement;
import de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht.ScheduleRow;
import de.unibielefeld.techfak.tdpe.prorema.utils.LocalDateInterval;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * @author Benedikt Volkmer
 *         Created on 6/20/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduleServiceImplTest {

    private Random random = new Random();

    private ScheduleService scheduleService;

    @Mock private WorksOnService worksOnService;
    @Mock private OrganisationUnitService organisationUnitService;
    @Mock private EmployeeService employeeService;
    @Mock private ProjectService projectService;

    @Before
    public void setUp() throws Exception {
        scheduleService = new ScheduleServiceImpl(employeeService, worksOnService, projectService,
                                                  organisationUnitService);
    }

    @Test
    public void getStatistics() throws Exception {

    }

    @Test
    public void getStandardData() throws Exception {

    }

    @Test
    public void filterByEmployee() throws Exception {

    }

    @Test
    public void filterByProject() throws Exception {

    }

    @Test
    public void filterByOrganisationUnit() throws Exception {

    }

    @Test
    public void getRows() throws Exception {
        // given
        Employee employee0 = mock(Employee.class);
        Employee employee1 = mock(Employee.class);
        Employee employee2 = mock(Employee.class);
        WorksOn worksOn00 = mock(WorksOn.class);
        WorksOn worksOn01 = mock(WorksOn.class);
        WorksOn worksOn02 = mock(WorksOn.class);
        WorksOn worksOn03 = mock(WorksOn.class);
        WorksOn worksOn10 = mock(WorksOn.class);
        WorksOn worksOn11 = mock(WorksOn.class);
        Project project0 = mock(Project.class);
        Project project1 = mock(Project.class);
        Project project2 = mock(Project.class);
        Project project3 = mock(Project.class);
        given(employeeService.getAll()).willReturn(Arrays.asList(employee0, employee1, employee2));
        given(worksOnService.getByDateAndEmployee(any(), any(), eq(0)))
                .willReturn(Arrays.asList(worksOn00, worksOn01, worksOn03));
        given(worksOnService.getByDateAndEmployee(any(), any(), eq(1)))
                .willReturn(Arrays.asList(worksOn10, worksOn11));
        given(worksOnService.getByDateAndEmployee(any(), any(), eq(2))).willReturn(Arrays.asList());
        given(projectService.findOne(0)).willReturn(project0);
        given(projectService.findOne(1)).willReturn(project1);
        given(projectService.findOne(2)).willReturn(project2);
        given(projectService.findOne(3)).willReturn(project3);
        given(employee0.getId()).willReturn(0);
        given(employee1.getId()).willReturn(1);
        given(employee2.getId()).willReturn(2);
        given(employee0.getWorkSchedule()).willReturn(40);
        given(employee1.getWorkSchedule()).willReturn(40);
        given(employee2.getWorkSchedule()).willReturn(40);
        given(worksOn00.getProjectId()).willReturn(0);
        given(worksOn01.getProjectId()).willReturn(1);
        given(worksOn02.getProjectId()).willReturn(2);
        given(worksOn03.getProjectId()).willReturn(3);
        given(worksOn10.getProjectId()).willReturn(0);
        given(worksOn11.getProjectId()).willReturn(1);
        given(worksOn00.getWorkload()).willReturn(40);
        given(worksOn01.getWorkload()).willReturn(25);
        given(worksOn02.getWorkload()).willReturn(15);
        given(worksOn03.getWorkload()).willReturn(15);
        given(worksOn10.getWorkload()).willReturn(40);
        given(worksOn11.getWorkload()).willReturn(40);
        given(worksOn00.getStatus()).willReturn(WorksOn.WorkStatus.BLOCKED);
        given(worksOn01.getStatus()).willReturn(WorksOn.WorkStatus.WORKING);
        given(worksOn02.getStatus()).willReturn(WorksOn.WorkStatus.BLOCKED);
        given(worksOn03.getStatus()).willReturn(WorksOn.WorkStatus.WORKING);
        given(worksOn10.getStatus()).willReturn(WorksOn.WorkStatus.BLOCKED);
        given(worksOn11.getStatus()).willReturn(WorksOn.WorkStatus.WORKING);
        // Overlaps at the end with proj3 and proj1
        given(worksOn00.getInterval())
                .willReturn(new LocalDateInterval(LocalDate.now().with(MONDAY),
                                                  LocalDate.now().plusWeeks(3).with(SUNDAY)));
        // Overlaps at the beginning with proj3 and proj0
        given(worksOn01.getInterval())
                .willReturn(new LocalDateInterval(LocalDate.now().plusWeeks(1).with(MONDAY),
                                                  LocalDate.now().plusWeeks(4).with(SUNDAY)));
        // Should not be shown
        given(worksOn02.getInterval())
                .willReturn(new LocalDateInterval(LocalDate.now().plusWeeks(5).with(MONDAY),
                                                  LocalDate.now().plusWeeks(6).with(SUNDAY)));
        // Is enclosed by proj0 and proj1
        given(worksOn03.getInterval())
                .willReturn(new LocalDateInterval(LocalDate.now().plusWeeks(2).with(MONDAY),
                                                  LocalDate.now().plusWeeks(2).with(SUNDAY)));
        given(worksOn10.getInterval())
                .willReturn(new LocalDateInterval(LocalDate.now().with(MONDAY),
                                                  LocalDate.now().plusWeeks(1).with(SUNDAY)));
        given(worksOn11.getInterval())
                .willReturn(new LocalDateInterval(LocalDate.now().with(MONDAY),
                                                  LocalDate.now().plusWeeks(1).with(SUNDAY)));
        /* Or visually:
                1 | 2 | 3 | 4 | 5 | 6 | 7 |
            00:|  40         |
            01:    |    25       |
            02:                    |     |
            03         | | 15
        */
        // when
        List<ScheduleRow> rows = scheduleService
                .getRows(new LocalDateInterval(LocalDate.now(), LocalDate.now().plusWeeks(4)),
                         Arrays.asList(employee0, employee1, employee2));

        // then
        //verify(worksOnService, VerificationModeFactory.times(4))
        //    .getByDateAndEmployee(eq(LocalDate.now()), eq(LocalDate.now().plusWeeks(4)), any());
        assertThat(rows).as("there are 3 employees").hasSize(3);
        ScheduleRow row0 = rows.get(0);
        assertThat(row0).isNotNull();
        assertThat(row0.getEmployee()).isEqualTo(employee0);
        assertThat(row0.getSubRows()).as("this employee works on 3 projects in this period").hasSize(3)
                                     .containsOnlyKeys(project0, project1, project3);
        assertThat(row0.getSubRows().get(project0)).hasSize(2).extracting("weekSpan", "status").containsExactly(
                tuple(4, WorksOn.WorkStatus.BLOCKED),
                tuple(1, WorksOn.WorkStatus.AVAILABLE));
        assertThat(row0.getSubRows().get(project1)).hasSize(2).extracting("weekSpan", "status").containsExactly(
                tuple(1, WorksOn.WorkStatus.AVAILABLE),
                tuple(4, WorksOn.WorkStatus.WORKING));
        assertThat(row0.getSubRows().get(project3)).hasSize(3).extracting("weekSpan", "status").containsExactly(
                tuple(2, WorksOn.WorkStatus.AVAILABLE),
                tuple(1, WorksOn.WorkStatus.WORKING),
                tuple(2, WorksOn.WorkStatus.AVAILABLE));
        assertThat(row0.getCondensedRow()).as("condensing results in 5 elements").hasSize(5)
                                          .extracting("weekSpan", "status", "load", "workload", "projects", "interval")
                                          .containsExactly(
                                                  tuple(1, WorksOn.WorkStatus.BLOCKED,
                                                        ScheduleElement.WorkloadState.IDEAL, 40,
                                                        Arrays.asList(project0),
                                                        new LocalDateInterval(LocalDate.now().with(MONDAY),
                                                                              LocalDate.now().plusWeeks(1)
                                                                                       .with(MONDAY))),
                                                  tuple(1, null, ScheduleElement.WorkloadState.MORE, 65,
                                                        Arrays.asList(project0, project1),
                                                        new LocalDateInterval(
                                                                LocalDate.now().plusWeeks(1).with(MONDAY),
                                                                LocalDate.now().plusWeeks(2).with(MONDAY))),
                                                  tuple(1, null, ScheduleElement.WorkloadState.MORE, 80,
                                                        Arrays.asList(project0, project1, project3),
                                                        new LocalDateInterval(
                                                                LocalDate.now().plusWeeks(2).with(MONDAY),
                                                                LocalDate.now().plusWeeks(2).with(SUNDAY))),
                                                  tuple(1, null, ScheduleElement.WorkloadState.MORE, 65,
                                                        Arrays.asList(project0, project1),
                                                        new LocalDateInterval(
                                                                LocalDate.now().plusWeeks(2).with(SUNDAY),
                                                                LocalDate.now().plusWeeks(3).with(SUNDAY))),
                                                  tuple(1, WorksOn.WorkStatus.WORKING,
                                                        ScheduleElement.WorkloadState.LESS, 25, Arrays.asList(project1),
                                                        new LocalDateInterval(
                                                                LocalDate.now().plusWeeks(3).with(SUNDAY),
                                                                LocalDate.now().plusWeeks(4).with(SUNDAY))));
        /* Or visually:
                     1 | 2 | 3 | 4 | 5 | 6 | 7 |
        condensed:  |   |   | |   |   |
        */

        ScheduleRow row1 = rows.get(1);
        assertThat(row1).isNotNull();
        assertThat(row1.getEmployee()).isEqualTo(employee1);
        assertThat(row1.getSubRows()).as("this employee works on 2 projects in this period").hasSize(2)
                                     .containsOnlyKeys(project0, project1);
        assertThat(row1.getSubRows().get(project0)).hasSize(2);
        assertThat(row1.getSubRows().get(project1)).hasSize(2);

        assertThat(row1.getCondensedRow()).as("condensing results in 2 elements").hasSize(2)
                                          .extracting("weekSpan", "status", "load", "workload", "projects", "interval")
                                          .containsExactly(
                                                  tuple(2, null,
                                                        ScheduleElement.WorkloadState.MORE, 80,
                                                        Arrays.asList(project0, project1),
                                                        new LocalDateInterval(LocalDate.now().with(MONDAY),
                                                                              LocalDate.now().plusWeeks(1)
                                                                                       .with(SUNDAY))),
                                                  tuple(3, WorksOn.WorkStatus.AVAILABLE,
                                                        ScheduleElement.WorkloadState.LESS, 0, new LinkedList<>(),
                                                        new LocalDateInterval(
                                                                LocalDate.now().plusWeeks(2).with(MONDAY),
                                                                LocalDate.now().plusWeeks(4).with(SUNDAY))));
    }

    @Test
    public void getMonthlyRows() throws Exception {
        // given
        Integer projectId = random.nextInt();
        WorksOn worksOn12 = mock(WorksOn.class);
        WorksOn worksOn23 = mock(WorksOn.class);
        WorksOn worksOn34 = mock(WorksOn.class);
        WorksOn worksOn45 = mock(WorksOn.class);
        WorksOn worksOn56 = mock(WorksOn.class);
        LocalDateInterval interval12 = new LocalDateInterval(LocalDate.now().withMonth(1).withDayOfMonth(1).with(MONDAY),
                                                             LocalDate.now().withMonth(2)
                                                                      .withDayOfMonth(15).with(SUNDAY));
        LocalDateInterval interval23 = new LocalDateInterval(interval12.getEnd().plusDays(1),
                                                             LocalDate.now().withMonth(3).withDayOfMonth(15).with(SUNDAY));
        LocalDateInterval interval34 = new LocalDateInterval(interval23.getEnd().plusDays(1),
                                                             LocalDate.now().withMonth(4).withDayOfMonth(15).with(SUNDAY));
        LocalDateInterval interval45 = new LocalDateInterval(interval34.getEnd().plusDays(1),
                                                             LocalDate.now().withMonth(5).withDayOfMonth(15).with(SUNDAY));
        LocalDateInterval interval56 = new LocalDateInterval(interval45.getEnd().plusDays(1),
                                                             LocalDate.now().withMonth(6).withDayOfMonth(15).with(SUNDAY));
        LocalDateInterval requestInterval = new LocalDateInterval(LocalDate.now().withDayOfYear(1),
                                                                  LocalDate.now().with(TemporalAdjusters.lastDayOfYear()));
        given(worksOn12.getInterval()).willReturn(interval12);
        given(worksOn23.getInterval()).willReturn(interval23);
        given(worksOn34.getInterval()).willReturn(interval34);
        given(worksOn45.getInterval()).willReturn(interval45);
        given(worksOn56.getInterval()).willReturn(interval56);
        given(worksOn12.getStatus()).willReturn(WorksOn.WorkStatus.ABSENCE);
        given(worksOn23.getStatus()).willReturn(WorksOn.WorkStatus.OFFERED);
        given(worksOn34.getStatus()).willReturn(WorksOn.WorkStatus.NOT_SPECIFIED);
        given(worksOn45.getStatus()).willReturn(WorksOn.WorkStatus.WORKING);
        given(worksOn56.getStatus()).willReturn(WorksOn.WorkStatus.BLOCKED);
        given(worksOn12.getProjectId()).willReturn(projectId);
        given(worksOn23.getProjectId()).willReturn(projectId);
        given(worksOn34.getProjectId()).willReturn(projectId);
        given(worksOn45.getProjectId()).willReturn(projectId);
        given(worksOn56.getProjectId()).willReturn(projectId);
        given(worksOnService.getByDateAndEmployee(any(LocalDate.class), any(LocalDate.class), anyInt()))
                .willReturn(Arrays.asList(worksOn12, worksOn23, worksOn34, worksOn45, worksOn56));
        Project project = mock(Project.class);
        given(projectService.findOne(projectId)).willReturn(project);
        given(project.getId()).willReturn(projectId);
        Employee employee = mock(Employee.class);
        // when
        Tuple<List<ScheduleRow>, List<YearMonth>> result = scheduleService.getMonthlyRows(requestInterval,
                                                                                          Arrays.asList(employee));
        // then
        List<ScheduleRow> rows = result.getLeft();
        assertThat(rows).hasSize(1);
        SortedSet<ScheduleElement> row = rows.get(0).getCondensedRow();
        assertThat(row).hasSize(12).extracting(ScheduleElement::getStatus)
                       .containsExactly(WorksOn.WorkStatus.ABSENCE, WorksOn.WorkStatus.OFFERED,
                                        WorksOn.WorkStatus.NOT_SPECIFIED, WorksOn.WorkStatus.WORKING,
                                        WorksOn.WorkStatus.BLOCKED, WorksOn.WorkStatus.AVAILABLE,
                                        WorksOn.WorkStatus.AVAILABLE, WorksOn.WorkStatus.AVAILABLE,
                                        WorksOn.WorkStatus.AVAILABLE, WorksOn.WorkStatus.AVAILABLE,
                                        WorksOn.WorkStatus.AVAILABLE, WorksOn.WorkStatus.AVAILABLE);
    }
}