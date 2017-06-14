package de.unibielefeld.techfak.tdpe.prorema.controller.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

/**
 * NewContactCmd.
 */
@Setter
@Getter
@NoArgsConstructor
public class NewContactCmd {

    private String nameTitle;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String email;

    private String tel;

    private String street;

    private String zip;

    private String city;

    private String country;

    @NotBlank
    @Min(0)
    private String clientId;

    private String id;
}
