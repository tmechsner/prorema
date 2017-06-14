package de.unibielefeld.techfak.tdpe.prorema.controller.command;

import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOnDetails;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frederik on 20.05.2016.
 */
@Getter
@Setter
public class NewWorksOnCmd {

    private Integer id;

    @NotBlank
    private Integer projectId;

    private List<Integer> employeeIds = new ArrayList<>();

    private String lastUrl;

    private Boolean permission;

    private Integer unitId;

    private Integer saveOverbooked;

    @NotBlank
    private List<WorksOnDetails> workDetails = new ArrayList<>();

    public NewWorksOnCmd() {
        this.workDetails.add(new WorksOnDetails());
    }

    public void removeEmployeeIds(List<Integer> employeeIds) {
        this.employeeIds.removeAll(employeeIds);
    }

    public void addEmployeeIds(List<Integer> employeeIds) {
        this.employeeIds.addAll(employeeIds);
    }

    public NewWorksOnCmd(WorksOn worksOn) {
        this.id = worksOn.getId();
        this.projectId = worksOn.getProjectId();
        this.employeeIds.add(worksOn.getEmployeeId());
        this.workDetails.add(new WorksOnDetails(0, worksOn.getWorkload(),
                worksOn.getStartDate().toString(), worksOn.getEndDate().toString(), worksOn.getStatus().toString()));
    }
}
