package de.unibielefeld.techfak.tdpe.prorema.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * This class enables mutliforms for WorksOns, so we can create many worksons at once.
 * Created by Frederik on 19.06.2016.
 */
@Getter
@Setter
public class WorksOnDetails {

    private int id;

    private int workload;

    //Dates have to be Strings here as it is not possible to read "LocalDate" from html. It is parsed in the service though.
    @NotBlank
    private String startDate;

    @NotBlank
    private String endDate;

    @NotBlank
    private String status;

    public WorksOnDetails() {
        this.id = 0;
    }

    public WorksOnDetails(int id, int workload, String startDate, String endDate, String status) {
        this.id = id;
        this.workload = workload;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
}
