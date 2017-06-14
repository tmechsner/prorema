package de.unibielefeld.techfak.tdpe.prorema.persistence;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;

/**
 * HasSkillEntity to determine if an Employee has a spetific skill.
 */
@Getter
@Setter
@RequiredArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity(name="HASSKILL")
public class HasSkillEntity {

    @Id
    @GeneratedValue
    @Column(name = "Employee_Skill_ID")
    private Integer id;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "SKILL_ID")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private SkillEntity skill;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "EMPLOYEE_ID")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private EmployeeEntity employee;

    @Column(name = "hasskilllevel")
    private String level;

    public HasSkillEntity(SkillEntity skill, EmployeeEntity employee, String level) {
        this.skill = skill;
        this.employee = employee;
        this.level = level;
    }

}
