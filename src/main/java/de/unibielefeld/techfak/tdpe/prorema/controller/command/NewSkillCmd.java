package de.unibielefeld.techfak.tdpe.prorema.controller.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Skillcmd.
 */
@Getter
@Setter
@NoArgsConstructor
public class NewSkillCmd {

    @NotBlank
    private String name;

    private String description;

    private String id;
}
