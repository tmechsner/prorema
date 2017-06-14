package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewSkillCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.SkillService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SkillControllerTest {
    @InjectMocks
    SkillController skillController;

    @Mock
    SkillService skillService;

    @Mock
    EmployeeService employeeService;

    @Mock
    ProjectService projectService;

    @Mock
    SimpleLockService simpleLockService;

    @Mock
    OrganisationUnitService organisationUnitService;

    @Mock
    OrganisationUnit organisationUnit;

    @Mock
    Employee currentUser;

    MockMvc mockMvc;

    private static Integer ID = 0;

    @Before
    public void setUp() throws PermissionDeniedException {
        MockitoAnnotations.initMocks(this);
        skillController = new SkillController(skillService, employeeService, projectService, simpleLockService);
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/templates/");
        irvr.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(skillController).setViewResolvers(irvr).build();

        LoginInfo.setLoginProvider(new TestLoginProvider(currentUser));
    }

    @Test
    public void showSkillsList() throws Exception {

        List<Skill> skill = mock(List.class);

        when(skillService.getAll()).thenReturn(skill);

        given(currentUser.getPosition()).willReturn(Employee.Position.ADMINISTRATOR);

        mockMvc.perform(get("/skillslist"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("listFragment"))
                .andExpect(model().attributeExists("gridFragment"))
                .andExpect(model().attributeExists("domain"))
                .andExpect(model().attribute("skills", skillService.getAll()))
                .andExpect(model().attribute("position", currentUser.getPosition().toString()))
                .andExpect(view().name("list"));

    }

    @Test
    public void showSkillForm() throws Exception {

        Skill skill = mock(Skill.class);

        when(skillService.findOne(anyInt())).thenReturn(skill);

        given(currentUser.getPosition()).willReturn(Employee.Position.ADMINISTRATOR);

        mockMvc.perform(get("/skillform")
                .param("id", ID.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("position", currentUser.getPosition().toString()))
                .andExpect(view().name("/skillform"));

    }

    @Test
    public void saveSkillForm() throws Exception {

  /**      Integer SKILL_ID = 0;
        String SKILL_NAME = "skill_name";
        String SKILL_DESCRIPTION = "skill_description";

        given(currentUser.getPosition()).willReturn(Employee.Position.ADMINISTRATOR);

            NewSkillCmd nouc = (NewSkillCmd) mockMvc
                    .perform(post("/skillform").param("name", SKILL_NAME))
                    .andExpect(status().isFound())
                    .andExpect(model().hasNoErrors())
                    .andExpect(model().attribute("newOrganisationUnitCmd",
                            Matchers.any(NewSkillCmd.class)))
                    .andExpect(redirectedUrl("/skill?id=" + SKILL_ID.toString()))
                    .andReturn().getModelAndView()
                    .getModel().get("newSkillCmd");
            assertThat(nouc).isNotNull().as("all params are valid")
                    .hasFieldOrPropertyWithValue("name", SKILL_NAME)
                    .hasFieldOrPropertyWithValue("description", SKILL_DESCRIPTION)
                    .hasFieldOrPropertyWithValue("id", SKILL_ID.toString());

            verify(simpleLockService).releaseLock(eq(new DomainIdentifier(SKILL_ID, OrganisationUnit.class)));

            mockMvc.perform(post("/skillform").param("id", "any_id")) //
                    .andExpect(status().isOk())
                    .andExpect(model().hasErrors())
                    .andExpect(model().attribute("newSkillCmd", Matchers.any(NewSkillCmd.class)))
                    .andExpect(view().name("skillform"));
*/

    }

    @Test
    public void showSkillProfil() throws Exception {

        List<Skill> skillList = mock(List.class);
        Skill skill = mock(Skill.class);
        Model model = mock(Model.class);
        List<Employee> employees = new ArrayList<Employee>();
        for (int i = 0; i < 10; i++) {
            Employee employee = mock(Employee.class);
            when(employee.getId()).thenReturn(2);
            when(employee.getWorkExit()).thenReturn(LocalDate.MAX);
            employees.add(employee);
        }

        when(skillService.findOne(anyInt())).thenReturn(skill);

        given(currentUser.getPosition()).willReturn(Employee.Position.ADMINISTRATOR);

        mockMvc.perform(get("/skill").param("id", ID.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("skill", skill))
                .andExpect(model().attribute("position", currentUser.getPosition().toString()))
                .andExpect(view().name("skillProfile"));
    }

}
