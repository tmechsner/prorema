package de.unibielefeld.techfak.tdpe.prorema.utils;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.NeedsSkill;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.NeedsSkillService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.SkillService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Benedikt Volkmer
 *         Created on 7/9/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SkillHelperTest {

    private Random random = new Random();

    @Mock private NeedsSkillService needsSkillService;

    private SkillHelper skillHelper;

    @Before
    public void setUp() throws Exception {
        skillHelper = new SkillHelper(needsSkillService);
    }

    @Test
    public void getNeedsSkillList() throws Exception {
        // given
        Integer projectId = random.nextInt();
        List<Integer> needsSkillIds = random.ints().boxed().limit(100).collect(Collectors.toList());
        List<Integer> skillIds = random.ints().boxed().limit(100).collect(Collectors.toList());
        List<Skill> skills = skillIds.stream().map(integer -> {
            Skill mock = mock(Skill.class, "id = " + integer);
            given(mock.getId()).willReturn(integer);
            return mock;
        }).collect(Collectors.toList());
        final Set<Integer> selectedSkillIds = new HashSet<>();
        List<NeedsSkill> needsSkills = needsSkillIds.stream().map(integer -> {
            NeedsSkill mock = mock(NeedsSkill.class, "id = " + integer);
            Skill skillMock = skills.get(random.nextInt(skills.size()));
            selectedSkillIds.add(skillMock.getId());
            given(mock.getId()).willReturn(integer);
            given(mock.getSkill()).willReturn(skillMock);
            given(mock.getSkillLevel()).willReturn(Skill.SkillLevel.values()[random.nextInt(3)]);
            skills.add(skillMock);
            return mock;
        }).collect(Collectors.toList());
        List<Integer> employeeIds = random.ints().boxed().limit(100).collect(Collectors.toList());
        List<Employee> employees = employeeIds.stream().map(integer -> {
            Employee mock = mock(Employee.class, "id = " + integer);
            given(mock.getId()).willReturn(integer);
            List<Skill> employeeSkills = skills.parallelStream().limit(10).collect(Collectors.toList());
            given(mock.getSkillList()).willReturn(
                    employeeSkills.stream()
                                  .map(skill -> new Tuple<>(skill, Skill.SkillLevel.values()[random.nextInt(3)]))
                                  .collect(Collectors.toList()));
            return mock;
        }).collect(Collectors.toList());
        given(needsSkillService.getSkillList(projectId)).willReturn(needsSkills);
        // when
        Map<Integer, Tuple<Integer, Skill.SkillLevel>> result = skillHelper.getNeedsSkillList(projectId, employees);
        // then
        assertThat(result.keySet()).doesNotContainNull()
                                   .containsOnlyElementsOf(selectedSkillIds);
        assertThat(result.values()).extracting(Tuple::getRight)
                                   .doesNotContainNull()
                                   .containsOnlyElementsOf(Arrays.asList(Skill.SkillLevel.values()));
    }

}