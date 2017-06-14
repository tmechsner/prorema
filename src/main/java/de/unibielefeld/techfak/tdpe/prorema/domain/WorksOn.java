package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.WorksOnEntity;
import de.unibielefeld.techfak.tdpe.prorema.utils.LocalDateInterval;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Created by David on 12.05.2016.
 */
@Getter
@Setter
public class WorksOn {

    private final Integer id;
    private final int projectId;
    private final int employeeId;
    private int workload;
    private WorkStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Constructor.
     *
     * @param entity Entity to create domain of
     */
    public WorksOn(WorksOnEntity entity) {
        this.id = entity.getId();
        this.workload = entity.getWorkload();
        this.employeeId = entity.getEmployee().getId();
        this.projectId = entity.getProject().getId();
        this.status = WorkStatus.fromString(entity.getStatus());
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
    }

    /**
     * Constructor.
     *
     * @param id         id of this worksOn
     * @param projectId  project id
     * @param employeeId employee id
     * @param workload   workload in this interval
     * @param status     status in this interval
     * @param startDate  start date of this interval
     * @param endDate    end date of this interval
     */
    public WorksOn(Integer id, int projectId, int employeeId, int workload, String status, LocalDate startDate,
                   LocalDate endDate) {
        this.id = id;
        this.projectId = projectId;
        this.employeeId = employeeId;
        this.workload = workload;
        this.status = WorkStatus.fromString(status);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setStatus(String status) {
        this.status = WorkStatus.fromString(status);
    }

    public LocalDateInterval getInterval() {
        return new LocalDateInterval(startDate, endDate);
    }

    /**
     * Hardcoded Status a working Period might have.
     * <p>The order is important for WorstCase calculation. Lowest is the worst case to show.</p>
     */
    public enum WorkStatus {
        /**
         * Possible status with Projects.
         */
        AVAILABLE("Disponierbar"), BLOCKED("Geblockt"), WORKING("Disponiert"),
        /**
         * Possible status without Projects.
         */
        NOT_SPECIFIED("Nicht angegeben"), OFFERED("Angeboten"), ABSENCE("Ausfallzeit");

        /**
         * German string representation of position.
         */
        private String nameDe;

        WorkStatus(String nameDe) {
            this.nameDe = nameDe;
        }

        @Override
        public String toString() {
            return nameDe;
        }

        /**
         * Return position to a german string representation.
         * Created by Frederik Kastner on 28.05.2016.
         *
         * @param nameDe german string
         * @return related position
         * @throws IllegalArgumentException if string is not known/assignable.
         */
        public static WorkStatus fromString(String nameDe) {
            for (WorkStatus element : WorkStatus.values()) {
                if (element.toString().equalsIgnoreCase(nameDe)) {
                    return element;
                }
            }
            throw new IllegalArgumentException("Position with name " + nameDe + " not known.");
        }

    }
}
