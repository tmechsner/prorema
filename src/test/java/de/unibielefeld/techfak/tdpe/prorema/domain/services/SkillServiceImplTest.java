package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewSkillCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.persistence.SkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.SkillRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * Created by Brian Baumbach on 29.05.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class SkillServiceImplTest {
    private static Integer SKILL_ID = 0;
    private static String SKILL_NAME = "skill name";
    private static String SKILL_DESCRIPTION = "skill description";

    private SkillServiceImpl skillServiceImpl;

    @Mock
    SkillRepository skillRepository;

    @Mock
    NewSkillCmd newSkillCmd;

    @Mock
    SkillEntity skillEntity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.skillServiceImpl = new SkillServiceImpl(skillRepository);

        when(newSkillCmd.getName()).thenReturn(SKILL_NAME);
        when(newSkillCmd.getDescription()).thenReturn(SKILL_DESCRIPTION);
        when(skillEntity.getId()).thenReturn(SKILL_ID);
        when(skillEntity.getName()).thenReturn(SKILL_NAME);
        when(skillEntity.getDescription()).thenReturn(SKILL_DESCRIPTION);
        when(skillRepository.findOne(SKILL_ID)).thenReturn(skillEntity);
    }

    @Test
    public void createSkill() throws Exception, PermissionDeniedException {
        // Test with existing id
        when(newSkillCmd.getId()).thenReturn(SKILL_ID.toString());
        when(skillRepository.exists(SKILL_ID)).thenReturn(true);
        when(skillRepository.save(skillEntity)).thenReturn(skillEntity);

        Skill skill = skillServiceImpl.createSkill(newSkillCmd);

        verify(newSkillCmd, atLeastOnce()).getId();
        verify(skillRepository).findOne(SKILL_ID);
        verify(skillEntity).setName(SKILL_NAME);
        verify(skillEntity).setDescription(SKILL_DESCRIPTION);

        assertThat(skill).isNotNull().as("existing id is given")
                .hasFieldOrPropertyWithValue("id", SKILL_ID)
                .hasFieldOrPropertyWithValue("name", SKILL_NAME)
                .hasFieldOrPropertyWithValue("description", SKILL_DESCRIPTION);

        //Test with non existing id
        when(newSkillCmd.getId()).thenReturn(SKILL_ID.toString());
        when(skillRepository.exists(SKILL_ID)).thenReturn(false);
        when(skillRepository.save(any(SkillEntity.class))).then(invocation -> {
            SkillEntity ret = (SkillEntity) invocation.getArguments()[0];
            ret.setId(SKILL_ID);
            return ret;
        });

        skill = skillServiceImpl.createSkill(newSkillCmd);

        verify(newSkillCmd, atLeastOnce()).getId();
        assertThat(skill).isNotNull().as("existing id is given")
                .hasFieldOrPropertyWithValue("id", SKILL_ID)
                .hasFieldOrPropertyWithValue("name", SKILL_NAME)
                .hasFieldOrPropertyWithValue("description", SKILL_DESCRIPTION);

        //Test without id
        when(newSkillCmd.getId()).thenReturn("");
        when(skillRepository.exists(SKILL_ID)).thenReturn(false);

        skill = skillServiceImpl.createSkill(newSkillCmd);

        verify(newSkillCmd, atLeastOnce()).getId();
        assertThat(skill).isNotNull().as("existing id is given")
                .hasFieldOrPropertyWithValue("id", SKILL_ID)
                .hasFieldOrPropertyWithValue("name", SKILL_NAME)
                .hasFieldOrPropertyWithValue("description", SKILL_DESCRIPTION);
    }
}
