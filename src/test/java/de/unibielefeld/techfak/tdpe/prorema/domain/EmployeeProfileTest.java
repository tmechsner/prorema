package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Alex Schneider on 29.05.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmployeeProfileTest {

    private static Integer PROFILE_ID = 0;
    private static Integer EMPLOYEE_ID = 0;
    private static String URL = "http:/path/to/profile.com/";

    @Mock
    EmployeeProfileEntity employeeProfileEntity;

    @Mock
    EmployeeEntity employeeEntity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void entityConstructor() throws Exception {
        when(employeeProfileEntity.getId()).thenReturn(PROFILE_ID);
        when(employeeProfileEntity.getUrl()).thenReturn(URL);
        when(employeeProfileEntity.getEmployee()).thenReturn(employeeEntity);

        EmployeeProfile employeeProfile = new EmployeeProfile(employeeProfileEntity);

        assertThat(employeeProfile).isNotNull().as("constructed from entity")
                .hasFieldOrPropertyWithValue("id", PROFILE_ID)
                .hasFieldOrPropertyWithValue("employeeId", EMPLOYEE_ID)
                .hasFieldOrPropertyWithValue("url", URL);
    }


}
