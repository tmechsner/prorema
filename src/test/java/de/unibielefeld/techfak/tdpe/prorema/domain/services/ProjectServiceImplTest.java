
package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewProjectCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.*;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.TestHelpers;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by timo on 27.04.16.
 */

public class ProjectServiceImplTest {


    /**
     * Dummy Data
     */

    private ProjectEntity projectEntity1 = new ProjectEntity("Großes Projekt", "Dies ist ein großes Projekt", "WON",
            null,null,null,null, BigDecimal.valueOf(1234), 10,
            true, null, LocalDate.now(),
            LocalDate.now().plusDays(1), 12, null, null, null);
    private ProjectEntity projectEntity2 = new ProjectEntity("Kleines Projekt", "Dies ist ein kleines Projekt",
            "WON",null,null,null,null, BigDecimal.valueOf(932), 74, true, null,
            LocalDate.now(), LocalDate.now().plusDays(1),
            32, null, null, null);
    private ProjectEntity projectEntity3 = new ProjectEntity("Verlorenes Projekt", "Dies ist ein verlorenes Projekt",
            "WON",null,null,null,null, BigDecimal.valueOf(100), 0,
            true, null, LocalDate.now(), LocalDate.now().plusDays(1),
            0, null, null, null);
    // Project domain objects
    private Project project1 = new Project(projectEntity1);
    private Project project2 = new Project(projectEntity2);
    private Project project3 = new Project(projectEntity3);

    // Raw project entities

    private Employee projectManager;
    private EmployeeEntity managerEntity;
    private OrganisationUnitEntity organisationUnitEntity = mock(OrganisationUnitEntity.class);
    private EmployeeProfileEntity employeeProfileEntity = mock(EmployeeProfileEntity.class);

    /**
     * Repos and Subject
     */


    private ProjectRepository projectRepository = mock(ProjectRepository.class);

    private EmployeeRepository employeeRepository = mock(EmployeeRepository.class);

    private EmployeeService employeeService = mock(EmployeeService.class);

    private SkillService skillService = mock(SkillService.class);

    private SkillRepository skillRepository = mock(SkillRepository.class);

    private NeedsSkillRepository needsSkillRepository = mock(NeedsSkillRepository.class);

    private ContactRepository contactRepository = mock(ContactRepository.class);

    private OrganisationUnitRepository organisationUnitRepository = mock(OrganisationUnitRepository.class);

    private ProjectService projectService;


    /**
     * Setup
     */

    @Before
    public void setUp() {
        //set the login to consultant as this is the only position allowed to see all projects
        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.SENIOR_MANAGER));

        when(organisationUnitEntity.getId()).thenReturn(0);
        managerEntity = new EmployeeEntity("title", "firstName", "lastName", "manager", "mail", "tel", LocalDate.now(),
                LocalDate.now().plusDays(1), "street", "zip", "city", "country", 0,
                "username", "password", organisationUnitEntity);
        managerEntity.setId(42);
        projectManager = new Employee(managerEntity);

        projectEntity1.setId(1);
        projectEntity2.setId(2);
        projectEntity3.setId(3);
        project1.setId(1);
        project2.setId(2);
        project3.setId(3);

        List<ProjectEntity> projectList = new ArrayList<>();
        projectList.add(projectEntity1);
        projectList.add(projectEntity2);
        projectList.add(projectEntity3);

        when(projectRepository.findAll()).thenReturn(projectList);

        when(projectRepository.findOne(1)).thenReturn(projectEntity1);
        when(projectRepository.findOne(2)).thenReturn(projectEntity2);
        when(projectRepository.findOne(3)).thenReturn(projectEntity3);

        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(projectEntity1);

        when(employeeService.findOne(managerEntity.getId())).thenReturn(projectManager);

        projectService = new ProjectServiceImpl(projectRepository, employeeRepository, employeeService,
                contactRepository, organisationUnitRepository, skillService, needsSkillRepository, skillRepository);
    }

    @Test
    public void testGetAll() throws Exception {
        List<Project> projects = projectService.getAll();
        assertTrue(projects.contains(project1));
        assertTrue(projects.contains(project2));
        assertTrue(projects.contains(project3));
    }

    @Test
    public void testFindOne() throws Exception {
        Project project = projectService.findOne(1);
        assertEquals(project, new Project(projectEntity1));

        project = projectService.findOne(2);
        assertEquals(project, new Project(projectEntity2));

        project = projectService.findOne(3);
        assertEquals(project, new Project(projectEntity3));
    }

    @Test
    public void testCreateProject() throws Exception, PermissionDeniedException {
        Project project = projectService.create(getNewProjectCmd());
        assertEquals(project, project1);
    }

    private NewProjectCmd getNewProjectCmd() {
        NewProjectCmd command = new NewProjectCmd();
        command.setName("Großes Projekt");
        command.setDescription("Dies ist ein großes Projekt");
        command.setStartDate(LocalDate.now().toString());
        command.setEndDate(LocalDate.now().plusDays(1).toString());
        command.setMenDays(123);
        command.setStatus("Won");
        command.setPotentialProjectVolume(BigDecimal.valueOf(1234.13));
        command.setConversionProbability(90);
        command.setProjectManagerId("1");
        command.setRunning("true");
        command.setContactId("1");
        return command;
    }

    @Test
    public void testCreateProjectNotAllowed() {
        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.PARTNER));
        try {
            projectService.create(getNewProjectCmd());
        } catch (PermissionDeniedException permissionDenied) {
            return;
        }
        fail("Exception not thrown");
    }

    @Test
    public void testDeleteProject() throws Exception {
        assertTrue(projectService.delete(1));

    }

    @Test
    public void testDeleteProjectnotAllowed() {
        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.PARTNER));
        assertFalse("Deletion should have been prohibited!", projectService.delete(1));
    }
}
