package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.AreaController;
import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewOrganisationUnitCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.locking.DomainIdentifier;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider.TestLoginProvider;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Int;
import static de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool.employee;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by Martin on 01.07.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AreaControllerTest {

    private static Integer ORGA_ID = 0;
    private static String ORGA_NAME = "orga_name";
    private static String ORGA_DESCRIPTION = "orga_description";
    private static Integer FIRST_MANAGER_ID = 0;
    private static Integer SECOND_MANAGER_ID = 1;
    private static Employee.Position CURRENT_USER_POSITION = Employee.Position.ADMINISTRATOR;


    @InjectMocks
    AreaController areaController;

    @Mock
    ProjectService projectService;

    @Mock
    EmployeeService employeeService;

    @Mock
    OrganisationUnitService organisationUnitService;

    @Mock
    SimpleLockService simpleLockService;

    @Mock
    OrganisationUnit organisationUnit;

    @Mock Employee currentUser;

    MockMvc mockMvc;

    @Before
    public void setUp() throws PermissionDeniedException {
        MockitoAnnotations.initMocks(this);
        areaController = new AreaController(projectService, employeeService, organisationUnitService, simpleLockService);
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/templates/");
        irvr.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(areaController).setViewResolvers(irvr).build();
        LoginInfo.setLoginProvider(new TestLoginProvider(currentUser));
        when(currentUser.getPosition()).thenReturn(CURRENT_USER_POSITION);

        when(organisationUnitService.findOne(ORGA_ID)).thenReturn(organisationUnit);
        when(organisationUnitService.findAll(anyCollection()))
                .thenReturn(Arrays.asList(organisationUnit, organisationUnit, organisationUnit));
        when(organisationUnitService.getAll())
                .thenReturn(Arrays.asList(organisationUnit, organisationUnit, organisationUnit));
        when(organisationUnitService.createOrganisationUnit(any(NewOrganisationUnitCmd.class)))
                .thenReturn(organisationUnit);
        when(organisationUnit.getId()).thenReturn(ORGA_ID);
        when(organisationUnit.getDescription()).thenReturn(ORGA_DESCRIPTION);
        when(organisationUnit.getFirstManagerId()).thenReturn(FIRST_MANAGER_ID);
        when(organisationUnit.getSecondManagerId()).thenReturn(SECOND_MANAGER_ID);
        when(organisationUnit.getEmployeeIdList()).thenReturn(Arrays.asList(0, 1, 2));
    }

    @Test
    public void areaProfile() throws Exception {

        Employee firstManager = mock(Employee.class);
        Employee secondManager = mock(Employee.class);
        List<Employee> employees = new ArrayList<Employee>();
        for (int i = 0; i < 10; i++) {
            Employee employee = mock(Employee.class);
            when(employee.getId()).thenReturn(2);
            when(employee.getWorkExit()).thenReturn(LocalDate.MAX);
            employees.add(employee);
        }

        List<Project> projects = new ArrayList<Project>();
        for (int i = 0; i < 3; i++)
            projects.add(mock(Project.class));


        when(employeeService.findOne(FIRST_MANAGER_ID)).thenReturn(firstManager);
        when(firstManager.getId()).thenReturn(FIRST_MANAGER_ID);
        when(employeeService.findOne(SECOND_MANAGER_ID)).thenReturn(secondManager);
        when(secondManager.getId()).thenReturn(SECOND_MANAGER_ID);
        when(employeeService.getByOrganisationUnit(any())).thenReturn(employees);
        when(projectService.getAll()).thenReturn(projects);

        //with parameter
        mockMvc.perform(get("/areaprofile").param("id", String.valueOf(ORGA_ID)))
               .andExpect(status().isOk())
               .andExpect(model().attribute("orgaUnit", organisationUnit))
               .andExpect(model().attribute("employees", employees))
               .andExpect(model().attribute("projects", projects))
               .andExpect(view().name("areaprofile"));

    }

    @Test
    public void showContactList() throws Exception {
        Employee firstManager = mock(Employee.class);
        Employee secondManager = mock(Employee.class);
        Map<Integer, Employee> firstmanager = new HashMap<>();
        Map<Integer, Employee> secondmanager = new HashMap<>();
        when(employeeService.findOne(FIRST_MANAGER_ID)).thenReturn(firstManager);
        when(employeeService.findOne(SECOND_MANAGER_ID)).thenReturn(secondManager);

        firstmanager.put(ORGA_ID, firstManager);
        secondmanager.put(ORGA_ID, secondManager);

        mockMvc.perform(get("/areas").param("id", String.valueOf(ORGA_ID)))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("listFragment"))
               .andExpect(model().attributeExists("gridFragment"))
               .andExpect(model().attributeExists("domain"))
               .andExpect(model().attributeExists("orgaUnits"))
               .andExpect(model().attribute("orgaUnits", Arrays.asList(organisationUnit, organisationUnit, organisationUnit)))
               .andExpect(model().attributeExists("firstManMap"))
               .andExpect(model().attribute("firstManMap", Matchers.equalTo(firstmanager)))
               .andExpect(model().attributeExists("secondManMap"))
               .andExpect(model().attribute("secondManMap", Matchers.equalTo(secondmanager)))
               .andExpect(view().name("list"));
    }

    @Test
    public void showClientForm() throws Exception {
        NewOrganisationUnitCmd nouc = (NewOrganisationUnitCmd) mockMvc.perform(get("/areaform"))
                                                                      .andExpect(status().isOk())
                                                                      .andExpect(model().attribute(
                                                                              "newOrganisationUnitCmd", Matchers.any(
                                                                                      NewOrganisationUnitCmd.class)))
                                                                      .andExpect(view().name("areaform"))
                                                                      .andReturn().getModelAndView().getModel()
                                                                      .get("newOrganisationUnitCmd");

        assertThat(nouc).isNotNull().as("no id is given")
                        .hasFieldOrPropertyWithValue("id", null);

        nouc = (NewOrganisationUnitCmd) mockMvc.perform(get("/areaform").param("id", String.valueOf(ORGA_ID)))
                                               .andExpect(status().isOk())
                                               .andExpect(model().attribute("newOrganisationUnitCmd",
                                                                            Matchers.any(NewOrganisationUnitCmd.class)))
                                               .andExpect(view().name("areaform"))
                                               .andReturn().getModelAndView().getModel().get("newOrganisationUnitCmd");

        assertThat(nouc).isNotNull().as("no id is given")
                        .hasFieldOrPropertyWithValue("id", String.valueOf(ORGA_ID));

        List<Employee> employees = new ArrayList<Employee>();
        for (int i = 0; i < 10; i++) {
            Employee employee = mock(Employee.class);
            when(employee.getId()).thenReturn(2);
            employees.add(employee);
        }

        when(employeeService.getByOrganisationUnit(any())).thenReturn(employees);
    }

    @Test
    public void saveProject() throws Exception {
        NewOrganisationUnitCmd nouc = (NewOrganisationUnitCmd) mockMvc
                .perform(post("/areaform").param("name", ORGA_NAME)
                                          .param("description", ORGA_DESCRIPTION)
                                          .param("id", ORGA_ID.toString())
                                          .param("firstManagerId", FIRST_MANAGER_ID.toString())
                                          .param("secondManagerId", SECOND_MANAGER_ID.toString()))
                .andExpect(status().isFound())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("newOrganisationUnitCmd",
                                             Matchers.any(NewOrganisationUnitCmd.class)))
                .andExpect(redirectedUrl("/areaprofile?id=" + ORGA_ID.toString()))
                .andReturn().getModelAndView()
                .getModel().get("newOrganisationUnitCmd");
        assertThat(nouc).isNotNull().as("all params are valid")
                        .hasFieldOrPropertyWithValue("name", ORGA_NAME)
                        .hasFieldOrPropertyWithValue("description", ORGA_DESCRIPTION)
                        .hasFieldOrPropertyWithValue("id", ORGA_ID.toString())
                        .hasFieldOrPropertyWithValue("firstManagerId", FIRST_MANAGER_ID.toString())
                        .hasFieldOrPropertyWithValue("secondManagerId", SECOND_MANAGER_ID.toString());
        verify(organisationUnitService).createOrganisationUnit(nouc);
        //Verify lock release
        verify(simpleLockService).releaseLock(eq(new DomainIdentifier(ORGA_ID, OrganisationUnit.class)));

        mockMvc.perform(post("/areaform").param("id", "nan")) //
               .andExpect(status().isOk())
               .andExpect(model().hasErrors())
               .andExpect(model().attribute("newOrganisationUnitCmd", Matchers.any(NewOrganisationUnitCmd.class)))
               .andExpect(view().name("areaform"));
    }
}
//kleine änderung um taggen zu können.
