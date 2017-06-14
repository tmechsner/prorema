package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.domain.services.*;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.utils.SkillHelper;
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

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectControllerTest {

    @InjectMocks
    ProjectController projectController;

    @Mock
    ProjectService projectService;

    @Mock
    ClientService clientService;

    @Mock
    ContactService contactService;

    @Mock
    OrganisationUnitService organisationUnitService;

    @Mock
    WorksOnService worksOnService;

    @Mock
    EmployeeService employeeService;

    @Mock
    SkillService skillService;

    @Mock
    ProjectEntity projectEntity;

    @Mock
    NeedsSkillService needsSkillService;

    @Mock
    SkillHelper skillHelper;

    @Mock
    SimpleLockService simpleLockService;

    private static Integer PROJECT_ID = 0;

    MockMvc mockMvc;

    @Before
    public void setUp() throws PermissionDeniedException {
        MockitoAnnotations.initMocks(this);
        projectController = new ProjectController(projectService, clientService, contactService, organisationUnitService,
                worksOnService, employeeService, skillService, needsSkillService, skillHelper, simpleLockService);
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/templates/");
        irvr.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(projectController).setViewResolvers(irvr).build();
    }

    @Test
    public void pipeline() throws Exception {

    }

    @Test
    public void singleproject() throws Exception {

    }

    @Test
    public void deleteProject() throws Exception {

    }

    @Test
    public void projectform() throws Exception {

    }

    @Test
    public void showProjectList() throws Exception {

    }

    @Test
    public void showProjectHistoryList() throws Exception {

    }

    @Test
    public void saveProject() throws Exception {

    }

    @Test
    public void convertProject() throws Exception {

    }

}
