package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewSkillCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.persistence.SkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;


/**
 * Service interface handling skill domains.
 */
public interface SkillService extends AbstactService<Skill , SkillEntity> {
    /**
     * create a skill from user input.
     * creates a database entry.
     *
     * @param command Command with user input
     * @return The created skill.
     */
    Skill createSkill(NewSkillCmd command) throws PermissionDeniedException;

}
