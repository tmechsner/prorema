package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.net.URL;

/**
 * Created by Alex Schneider.
 */
@Setter
@Getter
@RequiredArgsConstructor
public class EmployeeProfile {

    private Integer id;

    private Integer employeeId;

    @NonNull
    private String url;

    public EmployeeProfile(@NonNull EmployeeProfileEntity entity){
        this(entity.getUrl());
        this.id = entity.getId();
        employeeId = entity.getEmployee().getId();
    }


}
