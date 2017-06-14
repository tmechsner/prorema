package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewSkillCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.persistence.SkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.SkillRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Created by Jan Luca Offel on 16-04-28.
 */
@Service
@Primary
public class SkillServiceImpl extends AbstactServiceImpl<Skill, SkillRepository, SkillEntity>
        implements SkillService {
    /**
     * @param skillRepository SkillRepository
     */
    @Autowired
    public SkillServiceImpl(SkillRepository skillRepository) {
        super(skillRepository);
        accessDecider = AccessDeciderPool.skill;
    }

    @Override
    protected Skill init(SkillEntity entity) {
        return new Skill(entity);
    }

    @Override
    public Skill createSkill(NewSkillCmd command) throws PermissionDeniedException {
        if (command == null) {
            return null;
        }

        SkillEntity skillEntity;
        if ((command.getId() != null && !command.getId().isEmpty()) &&
                (repository.exists(Integer.valueOf(command.getId())))) {
            skillEntity = repository.findOne(Integer.valueOf(command.getId()));
            accessDecider.isAllowedThrow(Action.MODIFY, skillEntity);
            skillEntity.setName(command.getName());
            skillEntity.setDescription(command.getDescription());
        } else {
            accessDecider.isAllowedThrow(Action.CREATE, null);
            skillEntity = SkillEntity.builder()
                    .name(command.getName())
                    .description(command.getDescription())
                    .build();
        }
        SkillEntity saved = repository.save(skillEntity);
        return new Skill(saved);
    }

}
