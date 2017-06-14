package de.unibielefeld.techfak.tdpe.prorema.controller.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

/**
 * Command to create a new Employee.
 * Created by Benedikt Volkmer on 4/17/16.
 */
@Setter
@Getter
@Log4j2
@NoArgsConstructor
public class NewEmployeeCmd {

    private static List<String> skillList = new LinkedList<String>();

    private static List<String> skillLevelList = new LinkedList<String>();


    private List<String> removedSkillIds = new LinkedList<String>();
    private List<String> newSkillIds = new LinkedList<String>();

    private List<String> newSkillLevels = new LinkedList<String>();

    private List<String> removedProfiles = new LinkedList<>();

    private List<String> newProfiles = new LinkedList<>();

    private String id;

    private String nameTitle;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String sector;

    @NotBlank
    private String position;

    @NotBlank
    private String eMail;

    private String tel;

    private String username;

    private String password;

    private String workEntry;

    private String workExit;

    @Min(0)
    @Max(40)
    private Integer workSchedule;

    private String street;


    private String zip;

    private String city;

    private String country;

    @NumberFormat
    private String orgaUnitId;


    public static List<String> getSkillList() {
        return skillList;
    }

    public static void setSkillList(List<String> skillList) {
        NewEmployeeCmd.skillList = skillList;
    }

    public static List<String> getSkillLevelList() {
        return skillLevelList;
    }

    public static void setSkillLevelList(List<String> skillLevelList) {
        NewEmployeeCmd.skillLevelList = skillLevelList;
    }

    public String getWorkEntry() {
        if (this.workEntry != null) {
            return workEntry;
        } else
            return this.workEntry = LocalDate.now().toString();
    }

    public String getWorkExit() {
        if (this.workExit != null) {
            return workExit;
        } else {
            return this.workExit = LocalDate.MAX.toString();
        }
    }


}

