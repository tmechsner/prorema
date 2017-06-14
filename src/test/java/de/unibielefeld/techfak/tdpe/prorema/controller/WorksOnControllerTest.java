package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewWorksOnCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.*;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.*;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider.TestLoginProvider;
import de.unibielefeld.techfak.tdpe.prorema.utils.SkillHelper;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDate;
import java.util.*;

import static de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool.organisationUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WorksOnControllerTest {


    @InjectMocks
    WorksOnController worksOnController;

    @Mock
    WorksOnService worksOnService;

    @Mock
    ProjectService projectService;

    @Mock
    EmployeeService employeeService;

    @Mock
    NeedsSkillService needsSkillService;

    @Mock
    ClientService clientService;

    @Mock
    OrganisationUnitService organisationUnitService;

    @Mock
    SkillHelper skillHelper;

    @Mock
    Employee currentUser;

    MockMvc mockMvc;

    private static Integer ORGAUNITID = 0;
    private static Integer WORKSONID = 0;
    private static Integer PRJID = 0;
    private static Integer EMPLID = 0;
    private static Integer UNITID = 0;
    private static LocalDate startDate = LocalDate.now();

    @Before
    public void setUp() throws PermissionDeniedException {
        MockitoAnnotations.initMocks(this);
        worksOnController = new WorksOnController(worksOnService, projectService, employeeService,
                clientService, organisationUnitService, needsSkillService, skillHelper);
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/templates/");
        irvr.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(worksOnController).setViewResolvers(irvr).build();

        LoginInfo.setLoginProvider(new TestLoginProvider(currentUser));
    }

    @Test
    public void worksOnForm() throws Exception {
        WorksOn worksOn = mock(WorksOn.class);
        Employee firstManager = mock(Employee.class);
        Employee employeeOne = mock(Employee.class);
        Employee employeeTwo = mock(Employee.class);
        Employee employeeThree = mock(Employee.class);
        OrganisationUnit organisationUnit = mock(OrganisationUnit.class);
        List<Project> projects = mock(List.class);
        WorksOn worksOn1 = mock(WorksOn.class);
        WorksOn worksOn2 = mock(WorksOn.class);
        List<WorksOn> worksOnList = Arrays.asList(worksOn, worksOn1, worksOn2);
        Map<Integer, Tuple<Integer, Skill.SkillLevel>> skillMap = new HashMap<>();
        skillMap.put(0, new Tuple<>(0, Skill.SkillLevel.BEGINNER));
        skillMap.put(1, new Tuple<>(1, Skill.SkillLevel.EXPERT));
        skillMap.put(2, new Tuple<>(3, Skill.SkillLevel.ADVANCED));
        Skill skill1 = mock(Skill.class);
        Skill skill2 = mock(Skill.class);
        Skill skill3 = mock(Skill.class);
        List<Employee> employees = Arrays.asList(employeeOne, employeeTwo, employeeThree);
        List<Client> clients = mock(List.class);
        given(currentUser.getOrganisationUnitId()).willReturn(ORGAUNITID);
        given(worksOnService.findOne(WORKSONID)).willReturn(worksOn);
        given(worksOn.getStartDate()).willReturn(startDate);
        given(worksOn.getEndDate()).willReturn(LocalDate.MAX);
        given(worksOn.getStatus()).willReturn(WorksOn.WorkStatus.ABSENCE);
        given(organisationUnitService.findOne(UNITID)).willReturn(organisationUnit);
        given(organisationUnit.getFirstManagerId()).willReturn(0);
        given(employeeService.findOne(0)).willReturn(firstManager);
        given(currentUser.getPosition()).willReturn(Employee.Position.MANAGER);
        given(projectService.getAll()).willReturn(projects);
        given(employeeService.getAllNonExitedNonAdmin()).willReturn(employees);
        given(clientService.getAll()).willReturn(clients);
        given(organisationUnitService.findOne(anyInt())).willReturn(organisationUnit);
        given(organisationUnit.getName()).willReturn("testname");
        given(skillHelper.getNeedsSkillList(anyInt(), anyList())).willReturn(skillMap);
        given(worksOnService.getWorksOnByProject(anyInt())).willReturn(worksOnList);
        given(worksOn.getEmployeeId()).willReturn(0);
        given(worksOn.getEmployeeId()).willReturn(0);
        given(worksOn.getEmployeeId()).willReturn(0);
        given(skill1.getId()).willReturn(0);
        given(skill2.getId()).willReturn(1);
        given(skill3.getId()).willReturn(2);
        given(employeeOne.getSkillList()).willReturn(Arrays.asList(new Tuple<Skill, Skill.SkillLevel>(skill1, Skill.SkillLevel.BEGINNER)));
        given(employeeTwo.getSkillList()).willReturn(Arrays.asList(new Tuple<Skill, Skill.SkillLevel>(skill2, Skill.SkillLevel.BEGINNER)));
        given(employeeThree.getSkillList()).willReturn(Arrays.asList(new Tuple<Skill, Skill.SkillLevel>(skill3, Skill.SkillLevel.BEGINNER)));


        Map<String, Object> result = mockMvc.perform(get("/worksOnForm")
                .param("worksOnId", WORKSONID.toString())
                .param("prjId", PRJID.toString())
                .param("emplId", EMPLID.toString())
                .param("startDate", startDate.toString())
                .param("unitId", UNITID.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orgaUnitId", ORGAUNITID))
                .andExpect(model().attribute("states", Arrays.asList(WorksOn.WorkStatus.BLOCKED, WorksOn.WorkStatus.WORKING, WorksOn.WorkStatus.OFFERED, WorksOn.WorkStatus.ABSENCE)))
                .andExpect(model().attribute("responsible", firstManager))
                .andExpect(model().attribute("loginInfo", currentUser))
                .andExpect(model().attribute("projects", projects))
                .andExpect(model().attribute("employees", employees))
                .andExpect(model().attribute("clients", clients))
                .andExpect(model().attributeExists("newWorksOnCmd"))
                .andExpect(model().attributeExists("recommendation"))
                .andExpect(view().name("worksOnForm"))
                .andReturn().getModelAndView().getModel();


        assertThat(result.get("newWorksOnCmd")).hasFieldOrPropertyWithValue("projectId", PRJID)
                .hasFieldOrPropertyWithValue("unitId", UNITID)
                .hasFieldOrPropertyWithValue("permission", true);
        assertThat(((NewWorksOnCmd) result.get("newWorksOnCmd")).getEmployeeIds()).contains(EMPLID);
        assertThat(((HashMap<Integer, Integer>) result.get("recommendation"))).containsValues(161).containsKey(0);
        assertThat(((HashMap<Integer, Skill.SkillLevel>) result.get("notCoveredSkills"))).containsValues(Skill.SkillLevel.ADVANCED).containsKey(2);
        assertThat(((HashMap<Integer, Skill.SkillLevel>) result.get("lowCoveredSkills"))).containsValues(Skill.SkillLevel.EXPERT).containsKey(1);
        assertThat(((HashMap<Integer, Skill.SkillLevel>) result.get("fullyCoveredSkills"))).containsValues(Skill.SkillLevel.BEGINNER).containsKey(0);

    }

    @Test
    public void addRow() throws Exception {

        Employee firstManager = mock(Employee.class);
        List<Project> projects = mock(List.class);
        List<Employee> employees = mock(List.class);
        List<Client> clients = mock(List.class);
        OrganisationUnit organisationUnit = mock(OrganisationUnit.class);

        given(employeeService.findOne(0)).willReturn(firstManager);
        given(currentUser.getPosition()).willReturn(Employee.Position.MANAGER);
        given(currentUser.getOrganisationUnitId()).willReturn(ORGAUNITID);
        given(organisationUnit.getFirstManagerId()).willReturn(0);
        given(employeeService.getAllNonExitedNonAdmin()).willReturn(employees);
        given(projectService.getAll()).willReturn(projects);
        given(clientService.getAll()).willReturn(clients);

        mockMvc.perform(post("/worksOnForm").param("addDetails", "test"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orgaUnitId", ORGAUNITID))
                .andExpect(model().attribute("states", Arrays.asList(WorksOn.WorkStatus.BLOCKED, WorksOn.WorkStatus.WORKING, WorksOn.WorkStatus.OFFERED)))
                .andExpect(model().attribute("responsible", firstManager))
                .andExpect(model().attribute("loginInfo", currentUser))
                .andExpect(model().attribute("projects", projects))
                .andExpect(model().attribute("employees", employees))
                .andExpect(model().attribute("clients", clients))
                .andExpect(view().name("worksOnForm"))
                .andReturn().getModelAndView().getModel();

    }

//    @Test
//    public void removeRow() throws Exception {
//
//        mockMvc.perform(post("/worksOnForm").param("removeDetails", ""))
//                .andExpect(status().isOk())
//                .andExpect(view().name("worksOnForm"));
//
//
//    }

    @Test
    public void saveWorksOnForm() throws Exception {

    }

    @Test
    public void resourceIsLocked() throws Exception {

    }

}
