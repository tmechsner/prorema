package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewEmployeeCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.HasSkill;
import de.unibielefeld.techfak.tdpe.prorema.persistence.HasSkillEntity;

import java.util.List;

/**
 * Created by Mustermann on 06.06.2016.
 */
public interface HasSkillService extends AbstactService<HasSkill , HasSkillEntity> {

    void createHasSkills(NewEmployeeCmd command);

    void removeHasSkills(NewEmployeeCmd command);

    List<HasSkill> findByEmployeeId(int employeeId);
}
