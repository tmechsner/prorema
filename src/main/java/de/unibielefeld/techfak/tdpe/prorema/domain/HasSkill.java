package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.HasSkillEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Mustermann on 06.06.2016.
 */
@Setter
@Getter
@AllArgsConstructor
public class HasSkill {

    private final Integer id;
    private final Employee employee;
    private final Skill skill;
    private Skill.SkillLevel skillLevel;
}
