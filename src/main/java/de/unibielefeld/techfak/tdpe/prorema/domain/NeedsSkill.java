package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.NeedsSkillEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Mustermann on 06.06.2016.
 */
@Getter
@Setter
@AllArgsConstructor
public class NeedsSkill {
    private final Integer id;
    private final Project project;
    private final Skill skill;
    private Skill.SkillLevel skillLevel;
}
