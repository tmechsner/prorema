package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewEmployeeCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.*;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.TestHelpers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by Benedikt Volkmer on 4/17/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmployeeServiceImplTest {

    // Dummy input
    private static String NAMETITLE = "Title";
    private static String FIRSTNAME = "Max";
    private static String LASTNAME = "Testmann";
    private static String MAIL = "test@mail.test";
    private static String POSITION = "Senior Manager";
    private static String CITY = "TestCity";
    private static String COUNTRY = "Testlandia";
    private static String STREET = "TestStreet";
    private static String ZIP = "12345";
    private static String TEL = "129023219";
    private static String EMPLOYEE_PROFILE = "file:///profile.docx";
    private static int WORK_SCHEDULE = 100;
    private static String STATUS = "disponiert";
    private static String USERNAME = "testusername";
    private static String PASSWORD = "testpassword";
    private static String PASSWORD_HASH = "$2a$06$n0c1YI0zwN.1HOwK/piP6..BImuCEGkTphe/pyVcc3/Y4gE1HUDm.";
    private static String ROLE = "testrole";
    private static List<String> SKILL_LIST = Arrays.asList("skill1", "skill2", "skill3");
    private static String ORGANISATIONUNIT = "0";
    private static String WORK_ENTRY = "2016-01-21";
    private static String WORK_EXIT = "2017-01-24";

    private Random random = new Random();

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private HasSkillRepository hasSkillRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private OrganisationUnitRepository organisationUnitRepository;
    @Mock
    private EmployeeProfileRepository employeeProfileRepository;
    @Mock
    private SkillService skillService;
    @Mock
    private OrganisationUnitService organisationUnitService;
    @Mock
    private EmployeeProfileService employeeProfileService;
    private EmployeeService employeeService;
    private OrganisationUnitEntity organisationUnitEntity = mock(OrganisationUnitEntity.class);
    EmployeeEntity employeeEntity1 = new EmployeeEntity("Dr", "Max", "Mustermann", "Partner", "max@mustermann.test",
            "01234", LocalDate.now(),
            LocalDate.now().plusDays(1), "Str", "3344", "Bielefeld",
            "Deutschland", 20, "mmustermann",
            "passwd", organisationUnitEntity);
    EmployeeEntity employeeEntity2 = new EmployeeEntity("Dr", "Martina", "Musterfrau", "Senior Manager",
            "martina@musterfrau.test", "01234", LocalDate.now(),
            LocalDate.now().plusYears(1), "str2", "1122", "City", "Country",
            21, "mmusterfrau", "passwd", organisationUnitEntity);
    EmployeeEntity employeeEntity3 = new EmployeeEntity("Dr", "Valerie", "Müller", "Berater", "vallerie@müller.test",
            "01234", LocalDate.now(), LocalDate.now().plusMonths(1), "", "",
            "", "", 0, "vmueller", "passwd", organisationUnitEntity);
    private EmployeeProfileEntity employeeProfileEntity = mock(EmployeeProfileEntity.class);

    @Before
    public void setUp() {
        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.SENIOR_MANAGER));
        when(organisationUnitEntity.getId()).thenReturn(0);
        employeeService = new EmployeeServiceImpl(employeeRepository, hasSkillRepository, organisationUnitRepository,
                                                  skillRepository, skillService, employeeProfileService);
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(employeeEntity1, employeeEntity2, employeeEntity3));
        when(employeeRepository.findOne(any())).thenReturn(employeeEntity1);
    }

    @Test
    public void getAll() throws Exception {
        List<Employee> employees = employeeService.getAll();
        verify(employeeRepository).findAll();
        assertEquals(employees.size(), 3);
        assertEquals(employees.get(0), new Employee(employeeEntity1));
        assertEquals(employees.get(1), new Employee(employeeEntity2));
        assertEquals(employees.get(2), new Employee(employeeEntity3));

    }

    @Test
    public void findOne() throws Exception {
        Employee employee = employeeService.findOne(0);
        verify(employeeRepository).findOne(0);
        assertEquals(employee, new Employee(employeeEntity1));
    }

    @Test
    public void getFiltered() throws Exception {
        List<Employee> employees = employeeService.getFiltered(p -> p.getLastName().contains("Muster"));
        assertEquals(employees.size(), 2);
        assertEquals(employees.get(0), new Employee(employeeEntity1));
        assertEquals(employees.get(1), new Employee(employeeEntity2));
    }

    @Test
    public void testPositionChangeNotAllowed() throws Exception {
        create(Employee.Position.CONSULTANT, Employee.Position.CONSULTANT);
    }

    @Test
    public void testPositionChangeAllowed() throws Exception {
        TestHelpers.setLoginToPos(Employee.Position.ADMINISTRATOR);
        create(Employee.Position.SENIOR_MANAGER, Employee.Position.SENIOR_MANAGER);
    }

    public void create(Employee.Position expectedPoscretae, Employee.Position expectedPosMod) throws Exception, PermissionDeniedException {
        // This is what thymeleaf/spring does with the user input
        NewEmployeeCmd newEmployeeCmd = new NewEmployeeCmd();
        newEmployeeCmd.setNameTitle(NAMETITLE);
        newEmployeeCmd.setFirstName(FIRSTNAME);
        newEmployeeCmd.setLastName(LASTNAME);
        newEmployeeCmd.setSector(ORGANISATIONUNIT);
        newEmployeeCmd.setPosition(POSITION);
        newEmployeeCmd.setEMail(MAIL);
        newEmployeeCmd.setTel(TEL);
        newEmployeeCmd.setUsername(USERNAME);
        newEmployeeCmd.setPassword(PASSWORD);
        newEmployeeCmd.setWorkEntry(WORK_ENTRY);
        newEmployeeCmd.setWorkExit(WORK_EXIT);
        newEmployeeCmd.setWorkSchedule(WORK_SCHEDULE);
        newEmployeeCmd.setStreet(STREET);
        newEmployeeCmd.setCity(CITY);
        newEmployeeCmd.setZip(ZIP);
        newEmployeeCmd.setCountry(COUNTRY);
        NewEmployeeCmd.setSkillList(SKILL_LIST);
        newEmployeeCmd.setOrgaUnitId("0");


        //The actual test
        when(employeeRepository.save(any(EmployeeEntity.class)))
                .thenAnswer((Answer<EmployeeEntity>) i -> {
                    EmployeeEntity ret = (EmployeeEntity) i.getArguments()[0];
                    Integer id = ret.getId();
                    if (id == null) {
                        ret.setId(0); //As Id gets autoGenerated
                    }
                    return ret;
                });
        when(organisationUnitRepository.findAll()).thenAnswer(
                (Answer<Iterable<OrganisationUnitEntity>>) i -> {
                    OrganisationUnitEntity e = new OrganisationUnitEntity(ORGANISATIONUNIT, "dummy");
                    Integer id = e.getId();
                    if (id == null) {
                        e.setId(0); //As Id gets autoGenerated
                    }
                    return Arrays.asList(e);
                });
        OrganisationUnitEntity organisationUnitEntity = mock(OrganisationUnitEntity.class);
        when(organisationUnitEntity.getId()).thenReturn(Integer.valueOf(ORGANISATIONUNIT));
        when(organisationUnitRepository.findOne(Integer.valueOf(ORGANISATIONUNIT))).thenReturn(organisationUnitEntity);

        Employee employee = employeeService.create(newEmployeeCmd);

        verify(employeeRepository).save((EmployeeEntity) any());
        assertThat(employee)
                .extracting("id", "nameTitle", "firstName", "lastName", "position", "mail", "tel", "username",
                        "workEntry", "workExit", "workSchedule", "street", "zip", "city", "country",
                        "organisationUnitId")
                .containsExactly(0, NAMETITLE, FIRSTNAME, LASTNAME, expectedPoscretae, MAIL, TEL,
                        USERNAME, LocalDate.parse(WORK_ENTRY), LocalDate.parse(WORK_EXIT),
                        WORK_SCHEDULE, STREET, ZIP, CITY, COUNTRY, Integer.valueOf(ORGANISATIONUNIT));

        // given
        Integer id = random.nextInt();
        newEmployeeCmd.setId(id.toString());
        when(employeeRepository.exists(id)).thenReturn(true);
        ProjectEntity projectEntity = mock(ProjectEntity.class);
        EmployeeEntity repoEmployeeEntity = new EmployeeEntity("", "", "", "", "", "", "", "", "", "", -1,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1), "", "",
                organisationUnitEntity);
        repoEmployeeEntity.setId(id);
        repoEmployeeEntity.setPosition("Berater");
        when(employeeRepository.findOne(id)).thenReturn(repoEmployeeEntity);

        //when
        Employee resultEmployee = employeeService.create(newEmployeeCmd);

        //then
        assertThat(resultEmployee)
                .extracting("id", "nameTitle", "firstName", "lastName", "position", "mail", "tel", "username",
                        "workEntry", "workExit", "workSchedule", "street", "zip", "city", "country",
                        "organisationUnitId")
                .containsExactly(id, NAMETITLE, FIRSTNAME, LASTNAME, expectedPosMod, MAIL, TEL,
                        USERNAME, LocalDate.parse(WORK_ENTRY), LocalDate.parse(WORK_EXIT),
                        WORK_SCHEDULE, STREET, ZIP, CITY, COUNTRY, Integer.valueOf(ORGANISATIONUNIT));
        verify(employeeRepository, VerificationModeFactory.times(2)).save(any(EmployeeEntity.class));
    }

    @Test
    public void getByOrganisationUnit() throws Exception {
        // given
        int id = random.nextInt();

        // when
        employeeService.getByOrganisationUnit(id);

        // then
        verify(employeeRepository).findByOrganisationUnit_Id(id);
    }

    @Test
    public void getByPosition() throws Exception {
        //given
        String position = String.valueOf(random.nextDouble());

        // when
        employeeService.getByPosition(position);

        // then
        verify(employeeRepository).findByPosition(position);
    }

}
