package de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht;

import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import de.unibielefeld.techfak.tdpe.prorema.utils.LocalDateInterval;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

/**
 * @author Benedikt Volkmer
 *         Created on 6/19/16.
 */
@AllArgsConstructor
@Getter
public class ScheduleElement implements Comparable<ScheduleElement> {

    private static final int WORKDAYS = 5;

    /**
     * Period in weeks this element spans.
     */
    @NonNull
    private final Integer weekSpan;
    /**
     * Status of the worksOn of this period.
     * This is null, if this element represents multiple projects.
     */
    private final WorksOn.WorkStatus status;
    /**
     * Represents the state of workload of the employee in this period.
     */
    @NonNull
    private final WorkloadState load;
    /**
     * Hours of work per day in this period.
     */
    @NonNull
    private final Integer workload;
    /**
     * Projects this element condenses.
     */
    @NonNull
    private final List<Project> projects;
    /**
     * Interval of this element.
     */
    @NonNull
    private final LocalDateInterval interval;
    /**
     * WorksOn Id represented to this element.
     * <p> Null if this element represents multiple elements.</p>
     */
    @NonNull
    private final Integer worksOnId;

    @Override public int compareTo(ScheduleElement o) {
        return this.interval.compareTo(o.interval);
    }

    public static ScheduleElement ofDifferentInterval(ScheduleElement e, LocalDateInterval interval) {
        int weekSpan = interval.toPeriod() / 7;
        return new ScheduleElement(weekSpan, e.status, e.load, e.workload, e.projects, interval, e.worksOnId);
    }

    /**
     * Enum consisting workload states.
     */
    public enum WorkloadState {
        /**
         * State of workload.
         */
        LESS, IDEAL, MORE;

        /**
         * Get instance of given values.
         *
         * @param workload     current workload of the desired element
         * @param workSchedule work schedule of the employee the desired element is assigned to.
         * @return a new enum instance
         */
        public static WorkloadState ofData(Integer workload, Integer workSchedule) {
            if (workload.compareTo(workSchedule) < 0) {
                return WorkloadState.LESS;
            } else if (workload.compareTo(workSchedule) > 0) {
                return WorkloadState.MORE;
            } else { // workload == workSchedule / WORKDAYS
                return WorkloadState.IDEAL;
            }
        }
    }

}
