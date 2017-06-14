package de.unibielefeld.techfak.tdpe.prorema.controller.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * NewClientCmd.
 */
@Setter
@Getter
@NoArgsConstructor
public class NewClientCmd {

    @NotBlank
    private String name;

    private String description;
    private String location;

    private String id;
}
