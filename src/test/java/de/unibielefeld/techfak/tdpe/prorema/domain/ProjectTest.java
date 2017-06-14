package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Alex Schneider on 30.05.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectTest {

    private static Integer PROJECT_ID = 0;
    private static String NAME = "name";
    private static String DESCRIPTION = "description";
    private static String STATUS = "WON";
    private static BigDecimal PROJECT_VOL = BigDecimal.TEN;
    private static Integer CONV_PROPABILITY = 100;
    private static BigDecimal WEIGHTED_VOL = BigDecimal.TEN.setScale(2);
    private static Boolean ISRUNNING = true;
    private static Integer MENDAYS = 20;
    private static Integer PROJECTMANAGER_ID = null;
    private static LocalDate START_DATE = LocalDate.of(2000,1,1);
    private static LocalDate END_DATE = LocalDate.of(2001,1,1);

    @Mock
    ProjectEntity projectEntity;

    @Mock
    EmployeeEntity employeeEntity;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void entityConstructor() throws Exception {
        when(projectEntity.getId()).thenReturn(PROJECT_ID);
        when(projectEntity.getName()).thenReturn(NAME);
        when(projectEntity.getDescription()).thenReturn(DESCRIPTION);
        when(projectEntity.getStatus()).thenReturn(STATUS);
        when(projectEntity.getProjectVolume()).thenReturn(PROJECT_VOL);
        when(projectEntity.getConversionProbability()).thenReturn(CONV_PROPABILITY);
        when(projectEntity.getRunning()).thenReturn(ISRUNNING);
        when(projectEntity.getManDays()).thenReturn(MENDAYS);
        when(projectEntity.getStartDate()).thenReturn(START_DATE);
        when(projectEntity.getEndDate()).thenReturn(END_DATE);


        Project project = new Project(projectEntity);

        assertThat(project).isNotNull().as("constructed from entity")
                .hasFieldOrPropertyWithValue("name", NAME)
                .hasFieldOrPropertyWithValue("description", DESCRIPTION)
                .hasFieldOrPropertyWithValue("menDays", MENDAYS)
                .hasFieldOrPropertyWithValue("potentialProjectVolume", PROJECT_VOL)
                .hasFieldOrPropertyWithValue("conversionProbability", CONV_PROPABILITY)
//                .hasFieldOrPropertyWithValue("weightedProjectVolume", WEIGHTED_VOL)
                .hasFieldOrPropertyWithValue("running", ISRUNNING)
                .hasFieldOrPropertyWithValue("projectManagerId", PROJECTMANAGER_ID)
                .hasFieldOrPropertyWithValue("startDate", START_DATE)
                .hasFieldOrPropertyWithValue("endDate", END_DATE);
    }

}
