package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewOrganisationUnitCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.locking.DomainIdentifier;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * Wird nie benutzt, weil die originalklasse nicht gebraucht wird.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrganisationUnitControllerTest {

    private static Integer ORGA_ID = 0;
    private static String ORGA_NAME = "orga_name";
    private static String ORGA_DESCRIPTION = "orga_description";
    private static Integer FIRST_MANAGER_ID = 0;
    private static Integer SECOND_MANAGER_ID = 1;

    @InjectMocks
    OrganisationUnitController organisationUnitController;

    @Mock
    OrganisationUnitService organisationUnitService;

    @Mock
    EmployeeService employeeService;

    @Mock
    ProjectService projectService;

    MockMvc mockMvc;

    @Before
    public void setUp() throws PermissionDeniedException {
        MockitoAnnotations.initMocks(this);
        organisationUnitController = new OrganisationUnitController(organisationUnitService, employeeService, projectService);
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/templates/");
        irvr.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(organisationUnitController).setViewResolvers(irvr).build();
    }

    /**
     * nicht testbar, da das pazugehörige template nicht existiert.
    @Test
    public void showOrganisationUnitList() throws Exception {

        List<OrganisationUnit> organisationUnitList = mock(List.class);

        when(organisationUnitService.getAll()).thenReturn(organisationUnitList);

        mockMvc.perform(get("organisationUnitList"))
                .andExpect(status().isOk())
                .andExpect(view().name("organisationUnitList"));

    }
     */


    @Test
    public void showOrganisationUnitForm() throws Exception {

        Integer id = 0;

        OrganisationUnit organisationUnit = mock(OrganisationUnit.class);

        when(organisationUnitService.findOne(anyInt())).thenReturn(organisationUnit);

        mockMvc.perform(get("/organisationUnitform")
                .param("id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("/organisationUnitform"));


    }

    /**
     * Nicht testbar, da die verlinkung nicht existiert.
    @Test
    public void saveOrganisationUnitForm() throws Exception {

        NewOrganisationUnitCmd nouc = (NewOrganisationUnitCmd) mockMvc
                .perform(post("/organisationUnitform").param("name", ORGA_NAME)
                        .param("description", ORGA_DESCRIPTION)
                        .param("id", ORGA_ID.toString())
                        .param("firstManagerId", FIRST_MANAGER_ID.toString())
                        .param("secondManagerId", SECOND_MANAGER_ID.toString()))
                .andExpect(status().isFound())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("newOrganisationUnitCmd",
                        org.hamcrest.Matchers.any(NewOrganisationUnitCmd.class)))
                .andExpect(redirectedUrl("/organisationUnit" + ORGA_ID.toString()))
                .andReturn().getModelAndView()
                .getModel().get("newOrganisationUnitCmd");
        assertThat(nouc).isNotNull().as("all params are valid")
                .hasFieldOrPropertyWithValue("name", ORGA_NAME)
                .hasFieldOrPropertyWithValue("description", ORGA_DESCRIPTION)
                .hasFieldOrPropertyWithValue("id", ORGA_ID.toString())
                .hasFieldOrPropertyWithValue("firstManagerId", FIRST_MANAGER_ID.toString())
                .hasFieldOrPropertyWithValue("secondManagerId", SECOND_MANAGER_ID.toString());
        verify(organisationUnitService).createOrganisationUnit(nouc);

        mockMvc.perform(post("/organisationUnitform").param("id", "nan")) //
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("newOrganisationUnitCmd", org.hamcrest.Matchers.any(NewOrganisationUnitCmd.class)))
                .andExpect(view().name("organisationform"));

    }

    */

}
