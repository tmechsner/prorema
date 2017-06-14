package de.unibielefeld.techfak.tdpe.prorema.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by david on 09.06.2016.
 */
@Getter
@Setter
public class ProjectsAndManagerMails {

    private String employeeName;
    private Map<Project, String> projectEmailMap;

    public ProjectsAndManagerMails(String employeeName, Map<Project, String> projectMailMap) {
        this.employeeName = employeeName;
        this.projectEmailMap = projectMailMap;
    }

}
