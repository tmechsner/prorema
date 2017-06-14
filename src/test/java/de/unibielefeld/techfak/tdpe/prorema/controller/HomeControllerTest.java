package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.HomeController;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ScheduleService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.WorksOnService;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider.TestLoginProvider;
import de.unibielefeld.techfak.tdpe.prorema.security.TestHelpers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Created by Matthias on 7/3/16.
 */
public class HomeControllerTest {


    @InjectMocks
    HomeController homeController;

    @Mock
    ProjectService projectService;

    @Mock
    EmployeeService employeeService;

    @Mock
    WorksOnService worksOnService;

    @Mock
    ScheduleService scheduleService;

    @Mock
    Employee currentUser;


    MockMvc mockMvc;

    @Before
    public void setUp() throws PermissionDeniedException {
        MockitoAnnotations.initMocks(this);
        homeController = new HomeController(projectService, employeeService, worksOnService, scheduleService);
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/templates/");
        irvr.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(homeController).setViewResolvers(irvr).build();

        LoginInfo.setLoginProvider(new TestLoginProvider(currentUser));

    }


    @Test
    public void base() throws Exception {
    }

    @Test
    public void home() throws Exception {

        List<Project> projects = mock(List.class);

        given(currentUser.getPosition()).willReturn(Employee.Position.MANAGER);
        given(currentUser.getFirstName()).willReturn("testname");

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("homeName", currentUser.getFirstName()))
                .andExpect(model().attribute("position", currentUser.getPosition().toString()))
                .andExpect(view().name("home"));

    }

    @Test
    public void impressum() throws Exception {

        mockMvc.perform(get("/impressum"))
                .andExpect(status().isOk())
                .andExpect(view().name("impressum"));

    }

    @Test
    public void adminhome() throws Exception {

        given(currentUser.getPosition()).willReturn(Employee.Position.ADMINISTRATOR);
        given(currentUser.getFirstName()).willReturn("testname");

        mockMvc.perform(get("/adminhome"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("homeName", currentUser.getFirstName()))
                .andExpect(model().attribute("position", currentUser.getPosition().toString()))
                .andExpect(view().name("adminhome"));

    }

    @Test
    public void impressumLogin() throws Exception {

        given(currentUser.getPosition()).willReturn(Employee.Position.MANAGER);

        mockMvc.perform(get("/impressumLoggedIn"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("position", currentUser.getPosition().toString()))
                .andExpect(view().name("impressumLoggedIn"));
    }

    @Test
    public void support() throws Exception {

        Employee employee1 = mock(Employee.class);
        Employee employee2 = mock(Employee.class);
        Employee employee3 = mock(Employee.class);
        List<Employee> employees = Arrays.asList(employee1, employee2, employee3);

        given(employeeService.getByPosition(Employee.Position.ADMINISTRATOR.toString())).willReturn(employees);
        given(currentUser.getPosition()).willReturn(Employee.Position.ADMINISTRATOR);

        mockMvc.perform(get("/support"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("listFragment"))
                .andExpect(model().attributeExists("gridFragment"))
                .andExpect(model().attributeExists("domain"))
                .andExpect(model().attribute("position", currentUser.getPosition().toString()))
                .andExpect(view().name("list"));
    }


    @Test
    public void testAdmin() {
        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.ADMINISTRATOR));
        HomeController hc = new HomeController(null, null, null, null);
        assertEquals(hc.adminhome(new ExtendedModelMap()), hc.home(new ExtendedModelMap()));
    }
}