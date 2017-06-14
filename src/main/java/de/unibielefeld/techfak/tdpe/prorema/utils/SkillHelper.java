package de.unibielefeld.techfak.tdpe.prorema.utils;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.NeedsSkill;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.NeedsSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by timo on 09.06.16.
 */
@Service
public class SkillHelper {

    private NeedsSkillService needsSkillService;

    @Autowired
    public SkillHelper(NeedsSkillService needsSkillService) {
        this.needsSkillService = needsSkillService;
    }

    public Map<Integer, Tuple<Integer, Skill.SkillLevel>> getNeedsSkillList(int id, List<Employee> employees) {
        // Load required skills
        List<NeedsSkill> needsSkillList = needsSkillService.getSkillList(id);

        // Compute skill coverage

        //List of all skills covered by employees
        List<Tuple<Skill, Skill.SkillLevel>> coveredSkillList = new ArrayList<>();

        employees.forEach(employee -> {
            List<Tuple<Skill, Skill.SkillLevel>> skillList = employee.getSkillList();
            coveredSkillList.addAll(employee.getSkillList());
        });

        //Fetch skills and skill levels from needed skills
        Map<Integer, Tuple<Integer, Skill.SkillLevel>> neededSkillMap = new HashMap<>();
        needsSkillList.forEach(needsSkill -> {
            neededSkillMap.put(needsSkill.getSkill().getId(), new Tuple<>(3, needsSkill.getSkillLevel()));
        });

        //Remove all non-relevant skills from the list of employee skill
        coveredSkillList.forEach(skillSkillLevelTuple -> {
            int skillId = skillSkillLevelTuple.getLeft().getId();
            Skill.SkillLevel level = skillSkillLevelTuple.getRight();

            if (neededSkillMap.containsKey(skillId)) {
                int replacement;
                Skill.SkillLevel requiredLevel = neededSkillMap.get(skillId).getRight();

                if (neededSkillMap.get(skillId).getLeft() > (replacement = requiredLevel.matchesWith(level))) {
                    neededSkillMap.replace(skillId, new Tuple<>(replacement, requiredLevel));
                }
            }
        });

        return neededSkillMap;
    }
}
