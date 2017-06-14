package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewProjectCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.NeedsSkill;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.persistence.NeedsSkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.SkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.NeedsSkillRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ProjectRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.SkillRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Mustermann on 06.06.2016.
 */
@Service
@Primary
@Log4j2
public class NeedsSkillServiceImpl extends AbstactServiceImpl<NeedsSkill, NeedsSkillRepository, NeedsSkillEntity>
        implements NeedsSkillService {

    private SkillRepository skillRepository;
    private ProjectRepository projectRepository;

    private ProjectService projectService;
    private SkillService skillService;

    @Autowired
    public NeedsSkillServiceImpl(SkillRepository skillRepository, ProjectRepository projectRepository,
                                 NeedsSkillRepository needsSkillRepository, ProjectService projectService,
                                 SkillService skillService) {
        super(needsSkillRepository);
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
        this.projectService = projectService;
        this.skillService = skillService;
        accessDecider = AccessDeciderPool.needsSkill;
    }

    @Override
    protected NeedsSkill init(NeedsSkillEntity entity) {
        return new NeedsSkill(entity.getId(), projectService.findOne(entity.getProject().getId()),
                              skillService.findOne(entity.getSkill().getId()),
                              Skill.SkillLevel.fromString(entity.getLevel()));
    }

    @Override
    public void createNeedsSkills(NewProjectCmd command) {
        if (command != null) {
            ProjectEntity projectEntity = projectRepository.findOne(Integer.parseInt(command.getId()));
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

            List<SkillEntity> skills = null;
            try {
                skills = newSkillIds.stream().map(id -> skillRepository
                        .findOne(Integer.parseInt(id))) //skillService.createIfNonExistent(s))
                                    .filter(s -> s != null)
                                    .collect(Collectors.toList());
            } catch (NullPointerException exp) {
                log.info("Command object has no skill projectId");
            }

            if (skills != null) {
                List<NeedsSkillEntity> needsSkillEntities = new LinkedList<>();
                skills.forEach(entity -> needsSkillEntities
                        .add(new NeedsSkillEntity(projectEntity, entity,
                                              newSkillMap.get(entity.getId()))));
                accessDecider.isAllowedThrow(Action.MODIFY, null);
                repository.save(needsSkillEntities);
            }
        }
    }

    @Override
    public void removeNeedsSkills(NewProjectCmd command) {
        if (command != null) {
            command.getRemovedNeedsSkillIds().stream()
                   .filter(id -> id != null)
                   .forEach(id -> repository.delete(Integer.parseInt(id)));
        }
    }

    @Override
    public List<NeedsSkill> getSkillList(Integer projectId) {
        List<NeedsSkill> needsSkills = new LinkedList<>();
        repository.findByProject_Id(projectId).forEach(e -> {
            Skill.SkillLevel skillLevel = Skill.SkillLevel.fromString(e.getLevel());
            needsSkills.add(new NeedsSkill(e.getId(), projectService.findOne(e.getProject().getId()),
                                           skillService.findOne(e.getSkill().getId()), skillLevel));
        });
        return needsSkills;
    }


}
