package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.PdfController;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht.ScheduleRow;
import de.unibielefeld.techfak.tdpe.prorema.utils.LocalDateInterval;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * @author Benedikt Volkmer
 *         Created on 6/17/16.
 */
public interface ScheduleService {

    void getStatistics(Model model, Employee user, LocalDate start, LocalDate end, int weeks);

    /**
     * Equip employees with InformationPacker for given date range.
     *
     * @param displayFromDate  start date
     * @param displayUntilDate end date
     * @param displayWeeks     number of weeks to show
     * @return equipped employees
     * @deprecated only left for pdf creation
     */
    @Deprecated
    List<Employee> getStandardData(LocalDate displayFromDate, LocalDate displayUntilDate, int displayWeeks);

    @Deprecated
    List<Employee> filterByEmployee(LocalDate displayFromDate, LocalDate displayUntilDate, int displayWeeks, int emId);

    @Deprecated
    List<Employee> filterByProject(LocalDate displayFromDate, LocalDate displayUntilDate, int displayWeeks, int prjId);

    @Deprecated
    List<Employee> filterByOrganisationUnit(LocalDate displayFromDate, LocalDate displayUntilDate, int displayWeeks,
                                            int unitId);

    /**
     * Generate a list of ScheduleRows to display.
     *
     * @param interval  Interval of rows to generate
     * @param employees Employees to generate rows for
     * @return generated list of ScheduleRows
     */
    List<ScheduleRow> getRows(LocalDateInterval interval, List<Employee> employees);

    /**
     * Generate a list of ScheduleRows for a monthly view to display.
     *
     * @param employees employees to generate rows for
     * @param interval  Interval to generate rows of
     * @return generated list of ScheduleRows specialised for a year view and the affected months
     */
    Tuple<List<ScheduleRow>, List<YearMonth>> getMonthlyRows(LocalDateInterval interval, List<Employee> employees);

    /**
     * Returns PdfController, later used to create pdf.
     * @return the PdfController.
     */
    PdfController getPdfController();

    void getStatistics(Model model, Employee currentLogin, LocalDate start, LocalDate end, int weeks, int unitId);
}
