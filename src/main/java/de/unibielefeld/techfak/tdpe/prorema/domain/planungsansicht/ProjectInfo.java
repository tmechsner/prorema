package de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by david on 27.05.2016.
 */
@Getter
@Setter
@AllArgsConstructor
public class ProjectInfo {

    private int worksOnId;
    private int projectId;
    private String projectName;
    private int status;
}
