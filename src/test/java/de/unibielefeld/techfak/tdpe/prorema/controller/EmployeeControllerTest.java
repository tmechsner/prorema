package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.EmployeeController;
import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewEmployeeCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.EmployeeProfile;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.*;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@RunWith(MockitoJUnitRunner.class)
public class EmployeeControllerTest {

    private static Integer EMPLOYEE_ID = 0;
    private static String TITLE = "title";
    private static String EMPLOYEE_FIRST_NAME = "firstname";
    private static String EMPLOYEE_LAST_NAME = "lastname";
    private static String POSITION = "MANAGER";
    private static String EMAIL = "email";
    private static String TEL = "tel";
    private static String STREET = "street";
    private static String ZIP = "zip";
    private static String CITY = "city";
    private static String COUNTRY = "country";
    private static Integer WORKSCHEDULE = 0;
    private static LocalDate WORK_ENTRY = LocalDate.now();
    private static LocalDate WORK_EXIT = LocalDate.now().plusDays(1);
    private static String USERNAME = "username";
    private static String PASSWORD = "password";
    private static String ORGAUNIT_ID = "1";


    @InjectMocks
    EmployeeController employeeController;

    @Mock
    EmployeeService employeeService;

    @Mock
    Employee employee;

    @Mock
    EmployeeProfile employeeProfile;

    @Mock
    EmployeeProfileService employeeProfileService;

    @Mock
    OrganisationUnitService organisationUnitService;

    @Mock
    SkillService skillService;

    @Mock
    WorksOnService worksOnService;

    @Mock
    ProjectService projectService;

    @Mock
    HasSkillService hasSkillService;
    @Mock SimpleLockService simpleLockService;

    MockMvc mockMvc;

    @Before
    public void setUp() throws PermissionDeniedException {
        MockitoAnnotations.initMocks(this);
        employeeController = new EmployeeController(employeeService, employeeProfileService, worksOnService,
                                                    projectService, organisationUnitService, skillService,
                                                    hasSkillService, simpleLockService);
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/templates/");
        irvr.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).setViewResolvers(irvr).build();


        when(employeeService.findOne(EMPLOYEE_ID)).thenReturn(employee);
        when(employeeService.getAll()).thenReturn(Arrays.asList(employee, employee, employee));
        when(employeeService.create(any(NewEmployeeCmd.class))).thenReturn(employee);
        when(employee.getId()).thenReturn(EMPLOYEE_ID);
        when(employee.getFirstName()).thenReturn(EMPLOYEE_FIRST_NAME);
        when(employee.getLastName()).thenReturn(EMPLOYEE_LAST_NAME);
        when(employee.getPosition()).thenReturn(Employee.Position.fromString(POSITION));
        when(employee.getMail()).thenReturn(EMAIL);
        when(employee.getTel()).thenReturn(TEL);
        when(employee.getStreet()).thenReturn(STREET);
        when(employee.getZip()).thenReturn(ZIP);
        when(employee.getCity()).thenReturn(CITY);
        when(employee.getCountry()).thenReturn(COUNTRY);
        when(employee.getWorkSchedule()).thenReturn(WORKSCHEDULE);
        when(employee.getUsername()).thenReturn(USERNAME);
        when(employee.getPassword()).thenReturn(PASSWORD);
        when(employee.getOrganisationUnitId()).thenReturn(Integer.valueOf(ORGAUNIT_ID));
        when(employee.getWorkEntry()).thenReturn(WORK_ENTRY);
        when(employee.getWorkExit()).thenReturn(WORK_EXIT);
        when(employee.getHistory()).thenReturn(new ArrayList<ChangelogEntry>());
        when(employeeService.findAll(eq(Arrays.asList(0, 1, 2))))
                .thenReturn(Arrays.asList(employee, employee, employee));
        when(employee.getProjectManagers()).thenReturn(Arrays.asList(0, 1));
    }

    @Test
    public void userprofile() throws Exception {
        Set<EmployeeProfile> empProfiles = new LinkedHashSet<>();
        empProfiles.add(employeeProfile);

        mockMvc.perform(get("/userprofile").param("id", String.valueOf(EMPLOYEE_ID)))
               .andExpect(status().isOk())
               .andExpect(model().attribute("employee", employee))
               //.andExpect(model().attribute("profiles", empProfiles))
               .andExpect(view().name("userprofile"));

        mockMvc.perform(get("/userprofile"))
               .andExpect(view().name("error/error"));
    }


}
