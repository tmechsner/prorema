package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht.ScheduleRow;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ScheduleService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.WorksOnService;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.utils.LocalDateInterval;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 14.05.2016.
 */

@Controller
@RequestMapping("/schedule")
@Log4j2
public class ScheduleController extends ErrorHandler {

    private static final int DEFAULT_WEEK_RANGE = 12;

    private EmployeeService employeeService;
    private WorksOnService worksOnService;
    private ProjectService projectService;
    private OrganisationUnitService organisationUnitService;
    private final ScheduleService scheduleService;

    /**
     * Constructor.
     *
     * @param employeeService         autowired
     * @param worksOnService          autowired
     * @param projectService          autowired
     * @param organisationUnitService autowired
     * @param scheduleService         autowired
     */

    @Autowired
    public ScheduleController(EmployeeService employeeService, WorksOnService worksOnService,
                              ProjectService projectService, OrganisationUnitService organisationUnitService,
                              ScheduleService scheduleService) {
        this.employeeService = employeeService;
        this.worksOnService = worksOnService;
        this.projectService = projectService;
        this.organisationUnitService = organisationUnitService;
        this.scheduleService = scheduleService;
    }

    /**
     * Mapping for the schedule page.
     *
     * @param model     autowired
     * @param fromDate  get parameter
     * @param toDate    get parameter
     * @param projectId get parameter
     * @param unitId    get parameter
     * @param emId      get parameter
     * @param monthly   get parameter
     * @return schedule template
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String loadResources(Model model, String fromDate, String toDate, String projectId, String unitId,
                                String emId, String monthly) {
        try {
            LocalDate displayFromDate;
            LocalDate displayThruDate;

            if (fromDate == null || toDate == null) {
                displayFromDate = LocalDate.now().with(DayOfWeek.MONDAY);
                displayThruDate = displayFromDate.with(DayOfWeek.SUNDAY).plusWeeks(DEFAULT_WEEK_RANGE);
            } else {
                displayFromDate = LocalDate.parse(fromDate).with(DayOfWeek.MONDAY);
                displayThruDate = LocalDate.parse(toDate).with(DayOfWeek.SUNDAY);
            }

            Employee user = LoginInfo.getCurrentLogin();

            // Get timespan in weeks,
            // generate describing strings (e. g. "01.01. - 07.01.")
            int displayWeeks = 0;
            ArrayList<String> weekDatesStrings = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.");
            for (LocalDate dateIterator = displayFromDate; dateIterator.compareTo(
                    displayThruDate) <= 0; dateIterator = dateIterator.plusWeeks(1)) {
                displayWeeks++;
                String out = dateIterator.format(formatter) + " - " + dateIterator.with(DayOfWeek.SUNDAY)
                                                                                  .format(formatter);
                weekDatesStrings.add(out);
            }

            //get units without admins


            model.addAttribute("searchProjects", projectService.getByRunning(true));
            model.addAttribute("searchEmployees", employeeService.getAllNonExitedNonAdmin());
            model.addAttribute("units", organisationUnitService.getAllExceptAdminUnit());

            List<Employee> employees = new ArrayList<>();
            if (projectId == null && unitId == null && emId == null) {
                employees = scheduleService.getStandardData(displayFromDate, displayThruDate, displayWeeks);
            } else if (emId != null) {
                employees = scheduleService
                        .filterByEmployee(displayFromDate, displayThruDate, displayWeeks, Integer.parseInt(emId));
            } else if (projectId != null) {
                if (unitId != null) {
                    //TODO: Unit and project
                } else {
                    employees = scheduleService.filterByProject(displayFromDate, displayThruDate, displayWeeks,
                                                                Integer.parseInt(projectId));
                }
            } else if (unitId != null) {
                employees = scheduleService.filterByOrganisationUnit(displayFromDate, displayThruDate, displayWeeks,
                                                                     Integer.parseInt(unitId));
            }

            int selectedUnit;
            if (unitId != null) {
                selectedUnit = Integer.parseInt(unitId);
            } else {
                selectedUnit = -1;
            }
            scheduleService.getStatistics(model, user, displayFromDate, displayThruDate, displayWeeks, selectedUnit);

            model.addAttribute("employees", employees);
            model.addAttribute("position", LoginInfo.getPosition().toString());
            model.addAttribute("username", LoginInfo.getCurrentLogin().getUsername().toString());
            List<ScheduleRow> rows;
            if (employees.isEmpty()) {
                employeeService.getAllNonExitedNonAdmin();
            }
            if (!Boolean.valueOf(monthly)) {
                rows = scheduleService.getRows(new LocalDateInterval(displayFromDate, displayThruDate), employees);
                model.addAttribute("weekDates", weekDatesStrings);
            } else {
                Tuple<List<ScheduleRow>, List<YearMonth>> result = scheduleService
                        .getMonthlyRows(new LocalDateInterval(displayFromDate, displayThruDate), employees);
                rows = result.getLeft();
                scheduleService.getPdfController().update(result.getRight());
                model.addAttribute("monthly", true);
                model.addAttribute("months", result.getRight());
            }
            model.addAttribute("rows", rows);

            scheduleService.getPdfController().update(weekDatesStrings, employees, rows);
            try {
                scheduleService.getPdfController().calcEmployeeStats(LoginInfo.getCurrentLogin().getOrganisationUnitId(),
                        model, employeeService, worksOnService);
            } catch (NullPointerException ex) {
                //log.error(ex);
            }

            return "schedule";
        } catch (Exception e) {
            log.error("Fehler: " + e.getMessage());
            throw e;
        }
    }

}
