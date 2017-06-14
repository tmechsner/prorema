package de.unibielefeld.techfak.tdpe.prorema.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by timo on 07.06.16.
 */
@Getter
@Setter
@AllArgsConstructor
public class NeedsSkillAndCoverage {

    /**
     * Which skill is needed at which level
     */
    private final NeedsSkill needsSkill;

    /**
     * Coverage level: Difference between needed level (1 = beginner, 3 = expert) and level of the best employee.
     * -> 0 = good, 2 = bad coverage
     */
    private final int coverage;

}
