package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewEmployeeCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.HasSkill;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.HasSkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.SkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.HasSkillRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.SkillRepository;
import org.junit.Before;
import org.junit.Ignore;
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
 * Created by x4fyr on 7/1/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class HasSkillServiceImplTest {

    private Random random = new Random();

    @Mock private SkillRepository skillRepository;
    @Mock private SkillService skillService;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeService employeeService;
    @Mock private HasSkillRepository repository;

    private HasSkillServiceImpl service;

    @Before
    public void setUp() throws Exception {
        service = new HasSkillServiceImpl(skillRepository, skillService, employeeRepository, employeeService,
                                          repository);
    }

    @Test
    public void init() throws Exception {
        // given
        Integer id = random.nextInt();
        Integer employeeId = random.nextInt();
        Integer skillId = random.nextInt();
        Skill.SkillLevel skillLevel = Skill.SkillLevel.values()[random.nextInt(3)];
        HasSkillEntity entity = mock(HasSkillEntity.class);
        Employee employee = mock(Employee.class);
        Skill skill = mock(Skill.class);
        EmployeeEntity employeeEntity = mock(EmployeeEntity.class);
        SkillEntity skillEntity = mock(SkillEntity.class);
        given(employeeService.findOne(employeeId)).willReturn(employee);
        given(skillService.findOne(skillId)).willReturn(skill);
        given(entity.getEmployee()).willReturn(employeeEntity);
        given(employeeEntity.getId()).willReturn(employeeId);
        given(entity.getSkill()).willReturn(skillEntity);
        given(skillEntity.getId()).willReturn(skillId);
        given(entity.getId()).willReturn(id);
        given(entity.getLevel()).willReturn(skillLevel.toString());
        // when
        HasSkill result = service.init(entity);
        // then
        assertThat(result).extracting("id", "employee", "skill", "skillLevel")
                          .containsExactly(id, employee, skill, skillLevel);

    }

    @Test
    public void createHasSkills() throws Exception {
        // given
        Integer employeeId = random.nextInt();
        List<Integer> ids = random.ints().boxed().limit(10).collect(Collectors.toList());
        List<String> levels = new LinkedList<>();
        ids.forEach(s -> levels.add(Skill.SkillLevel.values()[random.nextInt(3)].toString()));
        NewEmployeeCmd command = mock(NewEmployeeCmd.class);
        given(command.getId()).willReturn(employeeId.toString());
        given(command.getNewSkillIds()).willReturn(ids.stream().map(Object::toString).collect(Collectors.toList()));
        given(command.getNewSkillLevels()).willReturn(levels);
        List<EmployeeEntity> employeeEntities = new LinkedList<>();
        given(employeeRepository.findOne(any())).willAnswer(invocation -> {
            Integer id = (Integer) invocation.getArguments()[0];
            EmployeeEntity mock = mock(EmployeeEntity.class);
            given(mock.getId()).willReturn(id);
            employeeEntities.add(mock);
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
        service.createHasSkills(command);
        // then
        ArgumentCaptor<List> hasSkillCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository, times(1)).save(hasSkillCaptor.capture());
        List<HasSkillEntity> result = hasSkillCaptor.getAllValues().get(0);
        assertThat(result).extracting("id").containsNull();
        assertThat(result).extracting("level").containsExactlyElementsOf(levels);
        assertThat(result).extracting("skill").containsExactlyElementsOf(skillEntities);
        assertThat(result).extracting("employee").containsOnlyElementsOf(employeeEntities);
    }

    @Test
    public void removeHasSkills() throws Exception {
        // given
        List<Integer> ids = random.ints().boxed().limit(100).collect(Collectors.toList());
        NewEmployeeCmd command = mock(NewEmployeeCmd.class);
        given(command.getRemovedSkillIds()).willReturn(ids.stream().map(Object::toString).collect(Collectors.toList()));
        // when
        service.removeHasSkills(command);
        // then
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(repository, times(100)).delete(idCaptor.capture());
        assertThat(idCaptor.getAllValues()).containsExactlyElementsOf(ids);
    }

}