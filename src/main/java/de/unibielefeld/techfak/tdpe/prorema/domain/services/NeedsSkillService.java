package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewProjectCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.NeedsSkill;
import de.unibielefeld.techfak.tdpe.prorema.persistence.NeedsSkillEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;

import java.util.List;

/**
 * Created by Mustermann on 06.06.2016.
 */
public interface NeedsSkillService extends AbstactService<NeedsSkill , NeedsSkillEntity> {

    void createNeedsSkills(NewProjectCmd command);

    void removeNeedsSkills(NewProjectCmd command);

    List<NeedsSkill> getSkillList(Integer projectId);
}
