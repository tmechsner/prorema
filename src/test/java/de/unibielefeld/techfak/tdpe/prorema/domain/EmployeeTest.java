package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Alex Schneider on 30.05.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmployeeTest {

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
    private static LocalDate WORK_ENTRY;
    private static LocalDate WORK_EXIT;
    private static String USERNAME = "username";
    private static String PASSWORD = "password";
    public static Integer ORGAUNITID = 0;

    @Mock
    EmployeeEntity employeeEntity;

    @Mock
    ProjectEntity projectEntity;

    @Mock
    EmployeeProfileEntity employeeProfileEntity;

    @Mock
    OrganisationUnitEntity organisationUnitEntity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void entityConstructor() throws Exception {
        when(employeeEntity.getId()).thenReturn(EMPLOYEE_ID);
        when(employeeEntity.getNameTitle()).thenReturn(TITLE);
        when(employeeEntity.getFirstName()).thenReturn(EMPLOYEE_FIRST_NAME);
        when(employeeEntity.getLastName()).thenReturn(EMPLOYEE_LAST_NAME);
        when(employeeEntity.getPosition()).thenReturn(POSITION);
        when(employeeEntity.getEmail()).thenReturn(EMAIL);
        when(employeeEntity.getTel()).thenReturn(TEL);
        when(employeeEntity.getStreet()).thenReturn(STREET);
        when(employeeEntity.getZip()).thenReturn(ZIP);
        when(employeeEntity.getCity()).thenReturn(CITY);
        when(employeeEntity.getCountry()).thenReturn(COUNTRY);
        when(employeeEntity.getWorkschedule()).thenReturn(WORKSCHEDULE);
        when(employeeEntity.getUsername()).thenReturn(USERNAME);
        when(employeeEntity.getPassword()).thenReturn(PASSWORD);
        when(employeeEntity.getOrganisationUnit()).thenReturn(organisationUnitEntity);
        when(employeeEntity.getWorkEntry()).thenReturn(WORK_ENTRY);
        when(employeeEntity.getWorkExit()).thenReturn(WORK_EXIT);


        Employee employee = new Employee(employeeEntity);

        assertThat(employee).isNotNull().as("constructed from entity")
                .hasFieldOrPropertyWithValue("id", EMPLOYEE_ID)
                .hasFieldOrPropertyWithValue("nameTitle", TITLE)
                .hasFieldOrPropertyWithValue("firstName", EMPLOYEE_FIRST_NAME)
                .hasFieldOrPropertyWithValue("lastName", EMPLOYEE_LAST_NAME)
                .hasFieldOrPropertyWithValue("mail", EMAIL)
                .hasFieldOrPropertyWithValue("tel", TEL)
                .hasFieldOrPropertyWithValue("street", STREET)
                .hasFieldOrPropertyWithValue("zip", ZIP)
                .hasFieldOrPropertyWithValue("city", CITY)
                .hasFieldOrPropertyWithValue("country", COUNTRY)
                .hasFieldOrPropertyWithValue("workSchedule", WORKSCHEDULE)
                .hasFieldOrPropertyWithValue("username", USERNAME)
                .hasFieldOrPropertyWithValue("password", PASSWORD)
                .hasFieldOrPropertyWithValue("organisationUnitId", ORGAUNITID)
                .hasFieldOrPropertyWithValue("workEntry", WORK_ENTRY)
                .hasFieldOrPropertyWithValue("workExit", WORK_EXIT);


    }

}
