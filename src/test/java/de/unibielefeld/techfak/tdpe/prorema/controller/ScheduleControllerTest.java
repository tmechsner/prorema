package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.*;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider.TestLoginProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduleControllerTest {

    @InjectMocks
    ScheduleController scheduleController;

    @Mock
    EmployeeService employeeService;

    @Mock
    WorksOnService worksOnService;

    @Mock
    ProjectService projectService;

    @Mock
    OrganisationUnitService organisationUnitService;

    @Mock
    ScheduleService scheduleService;

    @Mock
    Employee currentUser;

    MockMvc mockMvc;

    @Before
    public void setUp() throws PermissionDeniedException {
        MockitoAnnotations.initMocks(this);
        scheduleController = new ScheduleController(employeeService, worksOnService, projectService, organisationUnitService, scheduleService);
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/templates/");
        irvr.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController).setViewResolvers(irvr).build();

        LoginInfo.setLoginProvider(new TestLoginProvider(currentUser));
    }

    @Test
    public void loadResources() throws Exception {

        Employee employee1 = mock(Employee.class);
        Employee employee2 = mock(Employee.class);
        Employee employee3 = mock(Employee.class);
        List<Project> projectList = mock(List.class);
        List<Employee> employeeList = Arrays.asList(employee1, employee2, employee3);
        List<OrganisationUnit> organisationUnitList = mock(List.class);

        given(currentUser.getPosition()).willReturn(Employee.Position.ADMINISTRATOR);
        given(projectService.getByRunning(true)).willReturn(projectList);
        given(employeeService.getAllNonExitedNonAdmin()).willReturn(employeeList);
        given(organisationUnitService.getAllExceptAdminUnit()).willReturn(organisationUnitList);
        given(currentUser.getPosition()).willReturn(Employee.Position.ADMINISTRATOR);
        given(currentUser.getUsername()).willReturn("testname");

        mockMvc.perform(get("/schedule"))
                .andExpect(status().isOk())
              //  .andExpect(model().attribute("position", currentUser.getPosition().toString()))
              //  .andExpect(model().attribute("employees", employeeList))
                .andExpect(view().name("error/error"));

    }


}
