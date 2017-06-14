package de.unibielefeld.techfak.tdpe.prorema.controller.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Command for the archive, to get the information from the webpage
 */

@Setter
@Getter
@NoArgsConstructor
public class NewArchiveCmd {

    @NotBlank
    private String date;

    @NotBlank
    private String action;


}
