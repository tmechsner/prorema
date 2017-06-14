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
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Primary
@Log4j2
public class HasSkillServiceImpl extends AbstactServiceImpl<HasSkill, HasSkillRepository, HasSkillEntity>
        implements HasSkillService {

    private SkillRepository skillRepository;
    private SkillService skillService;
    private EmployeeRepository employeeRepository;
    private EmployeeService employeeService;

    @Autowired
    public HasSkillServiceImpl(SkillRepository skillRepository,
                               SkillService skillService,
                               EmployeeRepository employeeRepository,
                               EmployeeService employeeService,
                               HasSkillRepository hasSkillRepository) {
        super(hasSkillRepository);
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
        this.skillRepository = skillRepository;
        this.skillService = skillService;
        accessDecider = AccessDeciderPool.hasSkill;
    }


    @Override
    protected HasSkill init(HasSkillEntity entity) {
        Employee employee = employeeService.findOne(entity.getEmployee().getId());
        Skill skill = skillService.findOne(entity.getSkill().getId());
        return new HasSkill(entity.getId(), employee, skill, Skill.SkillLevel.fromString(entity.getLevel()));
    }

    @Override
    public void createHasSkills(NewEmployeeCmd command) {
        if (command != null) {
            EmployeeEntity employeeEntity = employeeRepository.findOne(Integer.parseInt(command.getId()));
            List<String> newSkillIds = command.getNewSkillIds();
            List<String> newSkillLevels = command.getNewSkillLevels();
            if (newSkillIds.size() != newSkillLevels.size()) {
                log.warn("SkillID and SkillLevel list sizes do not match!");
                throw new IllegalStateException("For each new skill there needs to be a skill level!");
            }

            Map<Integer, String> newSkillMap = new HashMap<>();
            for (int i = 0; i < newSkillIds.size(); i++) {
                String skillId = newSkillIds.get(i);
                String skillLevel = newSkillLevels.get(i);
                newSkillMap.put(Integer.parseInt(skillId), skillLevel);
            }

            List<SkillEntity> skills = new LinkedList<>();
            try {
                skills = newSkillIds.stream().map(id -> skillRepository
                        .findOne(Integer.parseInt(id))) //skillService.createIfNonExistent(s))
                                    .filter(s -> s != null)
                                    .collect(Collectors.toList());
            } catch (NullPointerException exp) {
                log.info("Command object has no skill id");
            }

            if (!skills.isEmpty()) {
                List<HasSkillEntity> hasSkillEntities = new LinkedList<>();
                skills.stream().forEach(e -> hasSkillEntities
                        .add(new HasSkillEntity(e, employeeEntity,
                                                newSkillMap.get(e.getId()))));
                accessDecider.isAllowedThrow(Action.MODIFY, null);
                repository.save(hasSkillEntities);
            }
        }
    }

    @Override public void removeHasSkills(NewEmployeeCmd command) {
        if(command != null) {
            command.getRemovedSkillIds().stream()
                   .filter(id -> id != null)
                   .forEach(id -> repository.delete(Integer.parseInt(id)));
        }
    }

    @Override
    public List<HasSkill> findByEmployeeId(int employeeId) {
        List<HasSkillEntity> entities = repository.findByEmployee_Id(employeeId);
        List<HasSkill> result = new ArrayList<>();
        entities.forEach(e -> result.add(init(e)));
        return result;
    }
}
