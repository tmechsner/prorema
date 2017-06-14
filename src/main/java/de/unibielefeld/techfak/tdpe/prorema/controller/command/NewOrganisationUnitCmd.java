package de.unibielefeld.techfak.tdpe.prorema.controller.command;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by Martin on 12.05.16.
 */
@Getter
@Setter
@NoArgsConstructor
public class NewOrganisationUnitCmd {

    private String id;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String firstManagerId;

    @NotBlank
    private String secondManagerId;
}
