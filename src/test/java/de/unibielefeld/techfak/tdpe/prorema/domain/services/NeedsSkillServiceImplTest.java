package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewProjectCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.NeedsSkill;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.persistence.HasSkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.NeedsSkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.SkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.NeedsSkillRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ProjectRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.SkillRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Benedikt Volkmer
 *         Created on 7/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class NeedsSkillServiceImplTest {

    private Random random = new Random();

    @Mock private SkillRepository skillRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectService projectService;
    @Mock private SkillService skillService;
    @Mock private NeedsSkillRepository repository;

    private NeedsSkillServiceImpl service;

    @Before
    public void setUp() throws Exception {
        service = new NeedsSkillServiceImpl(skillRepository, projectRepository, repository, projectService,
                                            skillService);
    }

    @Test
    public void init() throws Exception {
        // given
        Integer id = random.nextInt();
        Integer projectId = random.nextInt();
        Integer skillId = random.nextInt();
        NeedsSkillEntity entity = mock(NeedsSkillEntity.class);
        ProjectEntity projectEntity = mock(ProjectEntity.class);
        SkillEntity skillEntity = mock(SkillEntity.class);
        Project project = mock(Project.class);
        Skill skill = mock(Skill.class);
        Skill.SkillLevel level = Skill.SkillLevel.values()[random.nextInt(3)];
        given(entity.getId()).willReturn(id);
        given(entity.getProject()).willReturn(projectEntity);
        given(entity.getSkill()).willReturn(skillEntity);
        given(entity.getLevel()).willReturn(level.toString());
        given(projectEntity.getId()).willReturn(projectId);
        given(skillEntity.getId()).willReturn(skillId);
        given(projectService.findOne(projectId)).willReturn(project);
        given(skillService.findOne(skillId)).willReturn(skill);
        // when
        NeedsSkill result = service.init(entity);
        // then
        assertThat(result).extracting("id", "project", "skill", "skillLevel")
                          .containsExactly(id, project, skill, level);
    }

    @Test
    public void createNeedsSkills() throws Exception {
        // given
        Integer projectId = random.nextInt();
        List<Integer> ids = random.ints().boxed().limit(10).collect(Collectors.toList());
        List<String> levels = new LinkedList<>();
        ids.forEach(s -> levels.add(Skill.SkillLevel.values()[random.nextInt(3)].toString()));
        NewProjectCmd command = mock(NewProjectCmd.class);
        given(command.getId()).willReturn(projectId.toString());
        given(command.getNewSkillIds()).willReturn(ids.stream().map(Object::toString).collect(Collectors.toList()));
        given(command.getNewSkillLevels()).willReturn(levels);
        List<ProjectEntity> projectEntities = new LinkedList<>();
        given(projectRepository.findOne(any())).willAnswer(invocation -> {
            Integer id = (Integer) invocation.getArguments()[0];
            ProjectEntity mock = mock(ProjectEntity.class);
            given(mock.getId()).willReturn(id);
            projectEntities.add(mock);
            return mock;
        });
        List<SkillEntity> skillEntities = new LinkedList<>();
        given(skillRepository.findOne(any())).willAnswer(invocation -> {
            Integer id = (Integer) invocation.getArguments()[0];
            SkillEntity mock = mock(SkillEntity.class);
            given(mock.getId()).willReturn(id);
            skillEntities.add(mock);
            return mock;
        });
        // when
        service.createNeedsSkills(command);
        // then
        ArgumentCaptor<List> needSkillCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository, times(1)).save(needSkillCaptor.capture());
        List<NeedsSkillEntity> result = needSkillCaptor.getAllValues().get(0);
        assertThat(result).extracting("id").containsNull();
        assertThat(result).extracting("level").containsExactlyElementsOf(levels);
        assertThat(result).extracting("skill").containsExactlyElementsOf(skillEntities);
        assertThat(result).extracting("project").containsOnlyElementsOf(projectEntities);

    }

    @Test
    public void removeNeedsSkills() throws Exception {
        // given
        List<Integer> ids = random.ints().boxed().limit(100).collect(Collectors.toList());
        NewProjectCmd command = mock(NewProjectCmd.class);
        given(command.getRemovedNeedsSkillIds()).willReturn(ids.stream().map(Object::toString).collect(Collectors.toList()));
        // when
        service.removeNeedsSkills(command);
        // then
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(repository, times(100)).delete(idCaptor.capture());
        assertThat(idCaptor.getAllValues()).containsExactlyElementsOf(ids);
    }


}