package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * Created by Brian Baumbach on 29.05.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class SkillTest {

    private static Integer SKILL_ID = 0;
    private static String SKILL_NAME = "skill name";
    private static String SKILL_DESCRIPTION = "skill description";

    @Mock
    SkillEntity skillEntity;
    @Mock
    HasSkillEntity hasSkill1;
    @Mock
    HasSkillEntity hasSkill2;
    @Mock
    HasSkillEntity hasSkill3;
    @Mock
    EmployeeEntity employeeEntity1;
    @Mock
    EmployeeEntity employeeEntity2;
    @Mock
    EmployeeEntity employeeEntity3;
    @Mock
    NeedsSkillEntity needsSkill1;
    @Mock
    NeedsSkillEntity needsSkill2;
    @Mock
    NeedsSkillEntity needsSkill3;
    @Mock
    ProjectEntity projectEntity1;
    @Mock
    ProjectEntity projectEntity2;
    @Mock
    ProjectEntity projectEntity3;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void entityConstructor() throws Exception {
        when(skillEntity.getId()).thenReturn(SKILL_ID);
        when(skillEntity.getName()).thenReturn(SKILL_NAME);
        when(skillEntity.getDescription()).thenReturn(SKILL_DESCRIPTION);
        when(employeeEntity1.getId()).thenReturn(0);
        when(employeeEntity2.getId()).thenReturn(1);
        when(employeeEntity3.getId()).thenReturn(2);
        when(projectEntity1.getId()).thenReturn(0);
        when(projectEntity2.getId()).thenReturn(1);
        when(projectEntity3.getId()).thenReturn(2);
        when(hasSkill1.getEmployee()).thenReturn(employeeEntity1);
        when(hasSkill2.getEmployee()).thenReturn(employeeEntity2);
        when(hasSkill3.getEmployee()).thenReturn(employeeEntity3);
        when(needsSkill1.getProject()).thenReturn(projectEntity1);
        when(needsSkill2.getProject()).thenReturn(projectEntity2);
        when(needsSkill3.getProject()).thenReturn(projectEntity3);
        Set<HasSkillEntity> hasSkillEntities = new HashSet<>(
                Arrays.asList(hasSkill1, hasSkill2, hasSkill3));
        Set<NeedsSkillEntity> needsSkillEntities = new HashSet<>(
                Arrays.asList(needsSkill1, needsSkill2, needsSkill3));
        when(skillEntity.getHasSkills()).thenReturn(hasSkillEntities);
        when(skillEntity.getNeedsSkills()).thenReturn(needsSkillEntities);

        Skill skill = new Skill(skillEntity);

        assertThat(skill).as("constructed from entity")
                .hasFieldOrPropertyWithValue("id", SKILL_ID)
                .hasFieldOrPropertyWithValue("name", SKILL_NAME)
                .hasFieldOrPropertyWithValue("description", SKILL_DESCRIPTION);
        assertThat(skill.getEmployeeIds()).contains(0, 1, 2);
    }
}
