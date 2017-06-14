package de.unibielefeld.techfak.tdpe.prorema.controller.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.NumberFormat;

/**
 * Created by Alex Schneider.
 */

@Setter
@Getter
@NoArgsConstructor
public class NewEmployeeProfileCmd {

    private String id;

    @NotBlank
    @NumberFormat
    private String employeeId;

    private String url;
}
