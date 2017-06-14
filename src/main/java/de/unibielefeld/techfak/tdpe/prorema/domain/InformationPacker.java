package de.unibielefeld.techfak.tdpe.prorema.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn.WorkStatus;

import java.time.LocalDate;
import java.util.ArrayList;

@Getter
@Setter
public class InformationPacker {

    private int weekCount;
    private Integer worksOnId;
    private String prjName;
    private int prjId;
    private String status;
    private String startDate;

    public InformationPacker(Integer worksOnId, int weekCount, String prjName, int prjId, WorkStatus status) {
        this.worksOnId = worksOnId;
        this.weekCount = weekCount;
        this.prjName = prjName;
        this.prjId = prjId;
        this.status = status.toString();
    }

    public InformationPacker() {}

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate.toString();
    }

    public boolean equals(InformationPacker other) {
        if(other == null) {
            return false;
        }
        // Absicht! Damit in der Planungsansicht pro Woche ein "hinzufügen" Button angezeigt wird
        if(this.worksOnId == null && other.worksOnId == null) {
            return false;
        }
        if(this.worksOnId != null) {
            return this.worksOnId.equals(other.worksOnId);
        } else {
            return false;
        }
    }
}
