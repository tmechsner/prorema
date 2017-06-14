package de.unibielefeld.techfak.tdpe.prorema.domain;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by Alex Schneider.
 */

public interface FutureProject {

    /**
     * Checks the status of the project to decide if it is a future project.
     *
     * @param pos Status of project
     * @return boolean
     */
    boolean isFutureProject(Project.Status pos);

    Integer getId();

    void setId(Integer projectId);

    Integer getContactId();

    void setContactId(Integer contactId);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    LocalDate getStartDate();

    void setStartDate(LocalDate startDate);

    LocalDate getEndDate();

    void setEndDate(LocalDate endDate);

    Integer getMenDays();

    void setMenDays(Integer menDays);

    BigDecimal getPotentialProjectVolume();

    void setPotentialProjectVolume(BigDecimal potentialProjectVolume);

    Integer getConversionProbability();

    void setConversionProbability(Integer conversionProbability);

    BigDecimal getWeightedProjectVolume();

    String getZip();

    void setZip(String zip);

    String getStreet();

    void setStreet(String street);

    String getCity();

    void setCity(String city);

    String getCountry();

    void setCountry(String country);

}
