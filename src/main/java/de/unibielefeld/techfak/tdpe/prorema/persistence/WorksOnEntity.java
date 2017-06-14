package de.unibielefeld.techfak.tdpe.prorema.persistence;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;

import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDate;

import de.unibielefeld.techfak.tdpe.prorema.serializer.JsonDateDeserializer;
import de.unibielefeld.techfak.tdpe.prorema.serializer.JsonDateSerializer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by Brian on 17.04.2016.
 */
@Setter
@Getter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity(name="WORKSON")
public class WorksOnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Works_On_ID", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "EMPLOYEE_ID")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private EmployeeEntity employee;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "Project_ID")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private ProjectEntity project;

    @NonNull
    @Column(name = "status")
    private String status;

    @Column(name = "workload")
    private Integer workload;

    @Column(name = "startDate")
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private LocalDate startDate;

    @Column(name = "endDate")
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private LocalDate endDate;

    public WorksOnEntity(){
    }

    public WorksOnEntity(ProjectEntity project, EmployeeEntity employee, String status, Integer workload, LocalDate startDate, LocalDate endDate) {
        this.project = project;
        this.employee = employee;
        this.status = status;
        this.workload = workload;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @JsonDeserialize(using = JsonDateDeserializer.class)
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public LocalDate getStartDate() {
        return this.startDate;
    }

    @JsonDeserialize(using = JsonDateDeserializer.class)
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public LocalDate getEndDate() {
        return this.endDate;
    }
}
