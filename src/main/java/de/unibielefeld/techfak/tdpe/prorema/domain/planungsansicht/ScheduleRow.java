package de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.SortedSet;

/**
 * @author Benedikt Volkmer
 *         Created on 6/19/16.
 */
@AllArgsConstructor
@Getter
public class ScheduleRow {

    /**
     * Employee this row represents.
     */
    @NonNull
    private final Employee employee;
    /**
     * Row of schedule elements with projects condensed to one row.
     */
    @NonNull
    private final SortedSet<ScheduleElement> condensedRow;
    /**
     * Sub rows for every project separated, that are expandable.
     * <p> It should ideally be a linked map. </p>
     */
    @NonNull
    private final Map<Project, SortedSet<ScheduleElement>> subRows;

}
