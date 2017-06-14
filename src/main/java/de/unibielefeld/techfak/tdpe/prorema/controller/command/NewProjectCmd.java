package de.unibielefeld.techfak.tdpe.prorema.controller.command;

import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mrott on 26.04.16.
 */
@Setter
@Getter
public class NewProjectCmd {

    private String id;

    private String contactId;

    @NotBlank
    private String orgaUnitId;

    @NotBlank
    private String name;

    private String description;

    private String street;

    private String zip;

    private String city;

    private String country;

    private String startDate;

    private String endDate;

    @Min(0)
    private Integer menDays;

    @NotBlank
    private String status;

    @Min(0)
    private BigDecimal potentialProjectVolume;

    @Min(0)
    @Max(100)
    private Integer conversionProbability;

    private BigDecimal weightedProjectVolume;

    @NumberFormat
    private String projectManagerId;

    @NotBlank
    private String running;

    private List<String> newSkillIds = new LinkedList<>();

    private List<String> newSkillLevels = new LinkedList<>();

    private List<String> removedNeedsSkillIds = new LinkedList<>();

    public NewProjectCmd() {
    }

    public BigDecimal getWeightedProjectVolume(){
        if (this.potentialProjectVolume != null && this.conversionProbability !=null){
            this.weightedProjectVolume = potentialProjectVolume.multiply(new BigDecimal(conversionProbability).divide(new BigDecimal(100)));
            return weightedProjectVolume;
        } else
            return BigDecimal.ZERO;

    }
}
