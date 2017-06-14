package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.PdfController;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.InformationPacker;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht.ScheduleElement;
import de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht.ScheduleRow;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.utils.LocalDateInterval;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn.WorkStatus;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Benedikt Volkmer
 *         Created on 6/17/16.
 */
@Log4j2
@Service
@Primary
public class ScheduleServiceImpl implements ScheduleService {

    private static final int DAYS_IN_WEEK = 7;
    private static final int MONTH_IN_YEAR = 12;

    private final EmployeeService employeeService;
    private final WorksOnService worksOnService;
    private final ProjectService projectService;
    private final OrganisationUnitService organisationUnitService;
    private final PdfController pdfController;

    /**
     * Constructor.
     *
     * @param employeeService         autowired
     * @param worksOnService          autowired
     * @param projectService          autowired
     * @param organisationUnitService autowired
     */
    @Autowired
    public ScheduleServiceImpl(EmployeeService employeeService, WorksOnService worksOnService,
                               ProjectService projectService, OrganisationUnitService organisationUnitService) {
        this.employeeService = employeeService;
        this.worksOnService = worksOnService;
        this.projectService = projectService;
        this.organisationUnitService = organisationUnitService;
        this.pdfController = new PdfController();
    }

    @Override
    public void getStatistics(Model model, Employee user, LocalDate start, LocalDate end, int weeks) {
        getStatistics(model, user, start, end, weeks, -1);
    }

    public void getStatistics(Model model, Employee user, LocalDate start, LocalDate end, int weeks, int unitId) {

        List<Employee> employees;
        if (unitId != -1) {
            employees = employeeService.getByOrganisationUnit(unitId);
        } else {
            employees = employeeService.getAllExceptAdmins();
            log.debug("Logged in user has no Unit, computing stats for all projects and employees");
        }

        model.addAttribute("projectCount", projectService.getByRunning(true).size());

        //Employee count
        model.addAttribute("employeeCount", employees.size());

        //
        int underbookedEmployees = 0;
        Map<Integer, Integer> runningProjects = new HashMap<>();
        Map<Integer, Integer> worksOnWeekCounter = new HashMap<>();

        for (Employee employee : employees) {
            int[] worksonCounter = new int[weeks];
            List<WorksOn> worksOns = worksOnService.getByDateAndEmployee(start, end, employee.getId());
            worksOns.removeAll(worksOnService.getByStatus(Project.Status.PROPOSAL.toString().toLowerCase()));

            for (WorksOn worksOn : worksOns) {
                int prjId = worksOn.getProjectId();
                LocalDate toCheck;
                LocalDate endAnchor = worksOn.getEndDate().with(DayOfWeek.SUNDAY);
                if (worksOn.getStartDate().compareTo(start) < 0) {
                    toCheck = start; //if worksOn-startDate is already passed, start checkup at startDate
                } else {
                    toCheck = worksOn.getStartDate().with(DayOfWeek.MONDAY);
                }

                LocalDate weekIterator = start;
                for (int counter = 0; counter < weeks; counter++) { //Iterate timespan weeks
                    weekIterator = start.plusWeeks(counter);
                    if (weekIterator.compareTo(toCheck) >= 0 && weekIterator.compareTo(
                            endAnchor) <= 0) { //Add block to array
                        worksonCounter[counter] = worksonCounter[counter] + worksOn.getWorkload();
                        if (runningProjects.containsKey(prjId)) {
                            worksOnWeekCounter.replace(prjId, worksOnWeekCounter.get(prjId) + 1);
                            runningProjects.replace(prjId, runningProjects.get(prjId) + worksOn.getWorkload());
                        } else {
                            worksOnWeekCounter.put(prjId, 1);
                            runningProjects.put(prjId, worksOn.getWorkload());
                        }
                    }
                }
            }

            boolean underBooked = false;
            for (int amount : worksonCounter) {
                if (amount < employee.getWorkSchedule()) {
                    underBooked = true;
                }
            }

            if (underBooked) {
                underbookedEmployees++;
            }
        }

        int underbookedPrjs = 0;
        List<Integer> underbookedTotalIds = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : runningProjects.entrySet()) {
            int prjId = entry.getKey();
            int workload = entry.getValue();

            Project prj = projectService.findOne(prjId);
            LocalDate prjStart = prj.getStartDate();
            LocalDate prjEnd = prj.getEndDate();

            if (prjStart != null && prjEnd != null) {
                int prjWeeks = 0;
                for (LocalDate dateIterator = prjStart; dateIterator
                                                                .compareTo(prjEnd) <= 0; dateIterator = dateIterator
                        .plusWeeks(1)) {
                    prjWeeks++;
                }

                Integer menDays = prj.getMenDays() != null ? prj.getMenDays() : 0;
                int hoursPerWeek = menDays / prjWeeks * 5;
                int workloadPerWeek = workload / worksOnWeekCounter.get(prjId);

                if (hoursPerWeek > workloadPerWeek) {
                    underbookedPrjs++;
                    underbookedTotalIds.add(prjId);
                }
            }
        }

        //Retrieve which underbooked prjs come from pipeline
        List<Integer> pipelinePrjs = new ArrayList<>();
        projectService.getByRunning(false).forEach(project -> {
            if (project.getId() != 0) {
                pipelinePrjs.add(project.getId());
            }
        });

        pipelinePrjs.retainAll(underbookedTotalIds);

        pdfController.update(underbookedPrjs, pipelinePrjs.size(), underbookedEmployees, projectService.getByRunning(true).size());

        model.addAttribute("underbookedPrj", underbookedPrjs);
        model.addAttribute("underbookenPipeline", pipelinePrjs.size());
        model.addAttribute("underbookedEm", underbookedEmployees);

    }

    @Override
    public List<Employee> getStandardData(LocalDate displayFromDate, LocalDate displayUntilDate, int displayWeeks) {
        log.debug("No Organisationunit or project specified. Fetching standard Data.");
        List<Employee> employees = new ArrayList<>();
        for (Employee em : employeeService.getAllExceptAdmins()) {
            InformationPacker[][] infoInstances = new InformationPacker[10][displayWeeks];
            int parallelismIndex = 0;
            for (WorksOn worksOn : worksOnService.getByDateAndEmployee(displayFromDate, displayUntilDate, em.getId())) {
                log.trace("Found WorksOn with Id " + worksOn.getId());
                parallelismIndex = setWorksOnByDate(worksOn, parallelismIndex, displayFromDate, displayUntilDate,
                                                    displayWeeks, infoInstances);
            }

            fillUpEmptyWeeks(displayFromDate, displayWeeks, infoInstances, parallelismIndex);
            List<List<InformationPacker>> squeezedList = squeezeInfoPackerArray(infoInstances, parallelismIndex);
            em.setInformationPacker(squeezedList);
            employees.add(em);
        }

        return employees;
    }

    @Override
    public List<Employee> filterByEmployee(LocalDate displayFromDate, LocalDate displayUntilDate, int displayWeeks,
                                           int emId) {
        Employee employee = employeeService.findOne(emId);
        InformationPacker[][] infoInstances = new InformationPacker[10][displayWeeks];
        int parallelismIndex = 0;
        for (WorksOn worksOn : this.worksOnService.getByDateAndEmployee(displayFromDate, displayUntilDate, emId)) {
            parallelismIndex = setWorksOnByDate(worksOn, parallelismIndex, displayFromDate, displayUntilDate,
                                                displayWeeks, infoInstances);
        }

        fillUpEmptyWeeks(displayFromDate, displayWeeks, infoInstances, parallelismIndex);
        employee.setInformationPacker(squeezeInfoPackerArray(infoInstances, parallelismIndex));
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(employee);

        return employees;
    }

    @Override
    public List<Employee> filterByProject(LocalDate displayFromDate, LocalDate displayUntilDate, int displayWeeks,
                                          int prjId) {
        log.debug("Filtering data by project " + prjId);
        List<Employee> employees = new ArrayList<>();
        for (Employee em : employeeService.getAllExceptAdmins()) {
            InformationPacker[][] infoInstances = new InformationPacker[10][displayWeeks];
            boolean display = false;
            int parallelismIndex = 0;
            for (WorksOn worksOn : this.worksOnService
                    .getByDateAndEmployee(displayFromDate, displayUntilDate, em.getId())) {
                if (worksOn.getProjectId() == prjId) {
                    display = true;
                    log.trace("Found worksOn matching project " + prjId + ". Checking date...");
                    parallelismIndex = setWorksOnByDate(worksOn, parallelismIndex, displayFromDate, displayUntilDate,
                                                        displayWeeks, infoInstances);
                }
            }

            if (display) {
                fillUpEmptyWeeks(displayFromDate, displayWeeks, infoInstances, parallelismIndex);
                em.setInformationPacker(squeezeInfoPackerArray(infoInstances, parallelismIndex));
                employees.add(em);
            }
        }
        return employees;
    }

    @Override
    public List<Employee> filterByOrganisationUnit(LocalDate displayFromDate, LocalDate displayUntilDate,
                                                   int displayWeeks, int unitId) {
        log.debug("Filtering data by organisation unit " + unitId);
        ArrayList<Employee> employees = new ArrayList<>();

        log.debug("Fetching by employees belonging to the specified organisation unit");
        for (Employee em : employeeService.getAllExceptAdmins()) {
            if (em.getOrganisationUnitId() != null && em.getOrganisationUnitId() == unitId) {

                InformationPacker[][] infoInstances = new InformationPacker[10][displayWeeks];
                int parallelismIndex = 0;
                for (WorksOn worksOn : worksOnService
                        .getByDateAndEmployee(displayFromDate, displayUntilDate, em.getId())) {

                    log.trace("Found Works on with Id " + worksOn.getId());

                    parallelismIndex = setWorksOnByDate(worksOn, parallelismIndex, displayFromDate, displayUntilDate,
                                                        displayWeeks, infoInstances);
                }

                fillUpEmptyWeeks(displayFromDate, displayWeeks, infoInstances, parallelismIndex);
                em.setInformationPacker(squeezeInfoPackerArray(infoInstances, parallelismIndex));
                employees.add(em);
            }
        }

        return employees;
    }

    /**
     * Fill "infoInstances" with InformationPacker entries for the given WorksOn
     * from max(displayStartDate, worksOn.startDate) thru min(displayUntilDate, worksOn.endDate).
     *
     * @param worksOn          The WorksOn instance to be referenced in the informationPackers
     * @param parallelismIndex How many projects is the employee belonging to the WorksOn instance working on in
     *                         parallel?
     * @param displayStartDate Left limit of the schedule display
     * @param displayUntilDate Right limit of the schedule display
     * @param displayWeeks     How many weeks to be displayed in the schedule
     * @param infoInstances    The InformationPacker matrix for the employee belonging to the WorksOn.
     *                         (infoInstances[parallelism][weekOffset])
     * @return
     */
    private int setWorksOnByDate(WorksOn worksOn, int parallelismIndex, LocalDate displayStartDate,
                                 LocalDate displayUntilDate, int displayWeeks, InformationPacker[][] infoInstances) {
        // if worksOn.startDate is before displayStartDate, start setting infoInstances at displayStartDate
        LocalDate setRangeStart;
        if (worksOn.getStartDate().compareTo(displayStartDate) < 0) {
            setRangeStart = displayStartDate;
        } else {
            setRangeStart = worksOn.getStartDate().with(DayOfWeek.MONDAY);
        }

        // if worksOn.endDate is after displayUntilDate, stop setting infoInstances at displayUntilDate
        LocalDate setRangeEnd;
        if (worksOn.getEndDate().compareTo(displayUntilDate) > 0) {
            setRangeEnd = displayUntilDate;
        } else {
            setRangeEnd = worksOn.getEndDate().with(DayOfWeek.MONDAY);
        }

        String projectName = projectService
                .findOne(worksOn.getProjectId()).getName();
        LocalDate weekIterator = displayStartDate;

        int weekCount = (int) ChronoUnit.WEEKS.between(setRangeStart, setRangeEnd) + 1;
        for (int counter = 0; counter < displayWeeks; counter++) { //Iterate time span weeks
            weekIterator = displayStartDate.plusWeeks(counter);

            // Add block to array
            if (weekIterator.compareTo(setRangeStart) >= 0
                && weekIterator.compareTo(setRangeEnd) <= 0) {
                log.trace("Date matched entry at week " + counter + " for employee " + worksOn.getEmployeeId());
                if (infoInstances[parallelismIndex][counter] != null) {
                    parallelismIndex++;
                }
                infoInstances[parallelismIndex][counter] = new InformationPacker(worksOn.getId(), weekCount,
                                                                                 projectName,
                                                                                 worksOn.getProjectId(),
                                                                                 worksOn.getStatus());
            }
        }
        return parallelismIndex;
    }

    private void fillUpEmptyWeeks(LocalDate start, int displayWeeks, InformationPacker[][] infoInstances,
                                  int parallelismIndex) {
        for (int i = 0; i <= parallelismIndex; i++) {
            for (int weekOffset = 0; weekOffset < displayWeeks; weekOffset++) {
                if (infoInstances[i][weekOffset] == null) {
                    log.trace("Filling up empty space with 'available' blocks");
                    infoInstances[i][weekOffset] = new InformationPacker();
                    infoInstances[i][weekOffset].setStatus("Disponierbar");
                    infoInstances[i][weekOffset].setStartDate(start.plusWeeks(weekOffset));
                    infoInstances[i][weekOffset].setWeekCount(1);
                }
            }
        }
    }

    /**
     * Converts a matrix into a list of lists. <br/>
     * a a b b<br/>
     * b c c c<br/>
     * becomes [[a(2), b(2)], [b(1), c(3)]]
     *
     * @param infoInstances    The InformationPacker matrix.
     * @param parallelismIndex The size of infoInstances first dimension (empty dimensions are ignored!).
     * @return
     */
    private List<List<InformationPacker>> squeezeInfoPackerArray(InformationPacker[][] infoInstances,
                                                                 int parallelismIndex) {
        List<List<InformationPacker>> result = new LinkedList<>();
        for (int i = 0; i <= parallelismIndex; i++) {
            InformationPacker blockRepresentative = null;
            List<InformationPacker> partResult = new LinkedList<>();
            int blockContinuityCounter = 1;
            for (int weekOffset = 0; weekOffset < infoInstances[i].length - 1; weekOffset++) {
                if (infoInstances[i][weekOffset]
                            .equals(infoInstances[i][weekOffset + 1]) && (blockContinuityCounter % 4 != 0)) {
                    if (blockRepresentative == null) {
                        blockRepresentative = infoInstances[i][weekOffset];
                    }
                    blockContinuityCounter++;
                } else {
                    if (blockRepresentative != null) {
                        blockRepresentative.setWeekCount(blockContinuityCounter);
                        partResult.add(blockRepresentative);
                        blockRepresentative = infoInstances[i][weekOffset + 1];
                    } else {
                        infoInstances[i][weekOffset].setWeekCount(blockContinuityCounter);
                        partResult.add(infoInstances[i][weekOffset]);
                        blockRepresentative = infoInstances[i][weekOffset + 1];
                    }
                    blockContinuityCounter = 1;
                }
            }
            if (blockRepresentative != null) {
                blockRepresentative.setWeekCount(blockContinuityCounter);
                partResult.add(blockRepresentative);
            }
            result.add(partResult);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ScheduleRow> getRows(LocalDateInterval interval, List<Employee> employees) {
        Project absenceDummy = new Project(
                new ProjectEntity("Absence", null, WorkStatus.ABSENCE.toString(), null, null, null, null, null,
                                  null, true, null, null, null, null, null, null, null));
        absenceDummy.setId(-1);
        List<ScheduleRow> ret = new LinkedList<>();
        for (Employee employee : employees) {
            Map<Project, SortedSet<ScheduleElement>> subRows = new LinkedHashMap<>();
            List<ScheduleElement> condensedRowElements = new LinkedList<>();
            List<Integer> projectIds = new LinkedList<>();

            // Add create ScheduleElements from worksOns and add to lists
            for (WorksOn worksOn : worksOnService
                    .getByDateAndEmployee(interval.getStart(), interval.getEnd(), employee.getId())) {
                Project project;

                // Fill to sub row
                SortedSet<ScheduleElement> elements;
                if (worksOn.getStatus().equals(WorkStatus.ABSENCE)) {
                    project = absenceDummy;
                } else {
                    project = projectService.findOne(worksOn.getProjectId());
                }
                if (!(project.getStatus() == Project.Status.getByName("lost"))) {
                    if (!subRows.containsKey(project)) {
                        subRows.put(project, new TreeSet<>());
                        projectIds.add(project.getId());
                    }
                    elements = subRows.get(project);
                    Integer workload = worksOn.getWorkload();
                    Integer workSchedule = employee.getWorkSchedule();
                    LocalDateInterval worksOnInterval;
                    if (worksOn.getInterval().getStart().isBefore(interval.getStart())) {
                        if (worksOn.getInterval().getEnd().isAfter(interval.getEnd())) {
                            worksOnInterval = new LocalDateInterval(interval.getStart().with(DayOfWeek.MONDAY),
                                    interval.getEnd().with(DayOfWeek.SUNDAY));
                        } else {
                            worksOnInterval = new LocalDateInterval(interval.getStart().with(DayOfWeek.MONDAY),
                                    worksOn.getInterval().getEnd().with(DayOfWeek.SUNDAY));
                        }
                    } else {
                        if (worksOn.getInterval().getEnd().isAfter(interval.getEnd())) {
                            worksOnInterval = new LocalDateInterval(worksOn.getInterval().getStart().with(DayOfWeek.MONDAY),
                                    interval.getEnd().with(DayOfWeek.SUNDAY));
                        } else {
                            worksOnInterval = new LocalDateInterval(worksOn.getInterval().getStart().with(DayOfWeek.MONDAY),
                                    worksOn.getInterval().getEnd().with(DayOfWeek.SUNDAY));
                        }
                    }
                    Integer weekSpan = worksOnInterval.toPeriod() / DAYS_IN_WEEK;
                    ScheduleElement.WorkloadState state = ScheduleElement.WorkloadState.ofData(workload, workSchedule);

                    ScheduleElement element = new ScheduleElement(weekSpan, worksOn.getStatus(), state, workload,
                            Arrays.asList(project), worksOnInterval,
                            worksOn.getId());
                    elements.add(element);
                    condensedRowElements.add(element);
                }
            }
            if (subRows.isEmpty()) {
                LocalDateInterval gapInterval = new LocalDateInterval(LocalDate.now(), LocalDate.now().plusWeeks(1));
                ScheduleElement element = new ScheduleElement(gapInterval.toPeriod() / DAYS_IN_WEEK,
                                                              WorkStatus.AVAILABLE,
                                                              ScheduleElement.WorkloadState.LESS,
                                                              0,
                                                              new LinkedList<>(),
                                                              gapInterval,
                                                              -1);
                SortedSet<ScheduleElement> list = new TreeSet<>();
                list.add(element);
                subRows.put(absenceDummy, list);
            }


            // Condense row aka. eliminate overlaps.
            int i;
            boolean endReached = false;

            condense:
            while (!endReached) {
                Collections.sort(condensedRowElements);
                condensedRowElements = condensedRowElements.stream()
                                                           .filter(scheduleElement -> scheduleElement
                                                                                              .getWeekSpan() != 0)
                                                           .collect(Collectors.toList());

                for (i = 0; i < condensedRowElements.size() - 1; i++) { //Last element do not need to be compared
                    ScheduleElement curr = condensedRowElements.get(i);
                    ScheduleElement next = condensedRowElements.get(i + 1);
                    LocalDateInterval currInterval = curr.getInterval();
                    LocalDateInterval nextInterval = next.getInterval();

                    if (currInterval.equals(nextInterval)) {
                        assembleEqualElements(condensedRowElements, curr, next, employee);
                        continue condense;
                    } else if (nextInterval.encloses(currInterval)) {
                        separateEnclosingElements(condensedRowElements, next, curr, employee);
                        continue condense;
                    } else if (currInterval.encloses(nextInterval)) {
                        separateEnclosingElements(condensedRowElements, curr, next, employee);
                        continue condense;
                    } else if (currInterval.overlaps(nextInterval)) {
                        separateSimpleOverlappingElements(condensedRowElements, curr, next, employee);
                        continue condense;
                    }
                }
                endReached = true;
            }


            subRows.forEach((project, scheduleElements) -> fillGaps(scheduleElements, interval));


            Collections.sort(condensedRowElements);

            SortedSet<ScheduleElement> condensedRow = new TreeSet<>(condensedRowElements);

            fillGaps(condensedRow, interval);

            ret.add(new ScheduleRow(employee, condensedRow, subRows));
        }

        return ret;
    }

    @Override
    public Tuple<List<ScheduleRow>, List<YearMonth>> getMonthlyRows(LocalDateInterval interval,
                                                                    List<Employee> employees) {
        List<ScheduleRow> ret = new LinkedList<>();

        LocalDateInterval expandedInterval = new LocalDateInterval(
                interval.getStart().with(TemporalAdjusters.firstDayOfMonth()),
                interval.getEnd().with(TemporalAdjusters.lastDayOfMonth()));
        List<ScheduleRow> rows = getRows(expandedInterval, employees);

        // Generate a list of YearMonths
        List<YearMonth> months = new LinkedList<>();
        for (int yearInt = expandedInterval.getStart().getYear(); yearInt <= expandedInterval.getEnd()
                                                                                             .getYear(); yearInt++) {
            if (yearInt == expandedInterval.getStart().getYear() && yearInt == expandedInterval.getEnd().getYear()) {
                for (int monthInt = expandedInterval.getStart().getMonthValue();
                     monthInt <= expandedInterval.getEnd().getMonthValue(); monthInt++) {
                    months.add(YearMonth.of(yearInt, monthInt));
                }
            } else if (yearInt == expandedInterval.getStart().getYear()) {
                for (int monthInt = expandedInterval.getStart()
                                                    .getMonthValue(); monthInt <= MONTH_IN_YEAR; monthInt++) {
                    months.add(YearMonth.of(yearInt, monthInt));
                }
            } else if (yearInt == expandedInterval.getEnd().getYear()) {
                for (int monthInt = 1; monthInt <= expandedInterval.getEnd().getMonthValue(); monthInt++) {
                    months.add(YearMonth.of(yearInt, monthInt));
                }
            } else {
                for (int monthInt = 1; monthInt <= MONTH_IN_YEAR; monthInt++) {
                    months.add(YearMonth.of(yearInt, monthInt));
                }
            }
        }

        for (ScheduleRow row : rows) {
            Set<Project> projects = row.getSubRows().keySet();
            SortedSet<ScheduleElement> outRow = new TreeSet<>();

            for (YearMonth month : months) {
                LocalDateInterval monthInterval = new LocalDateInterval(month.atDay(1), month.atEndOfMonth());
                WorkStatus worstCase = WorkStatus.ABSENCE;


                final SortedSet<ScheduleElement> monthRow = new TreeSet<>();
                row.getCondensedRow().parallelStream()
                   .filter(scheduleElement -> monthInterval.overlaps(scheduleElement.getInterval()))
                   .forEach(monthRow::add);
                AcquireWorstCase:
                {
                    for (ScheduleElement scheduleElement : monthRow) {
                        if (scheduleElement.getStatus() != null && scheduleElement.getStatus()
                                                                                  .equals(WorkStatus.AVAILABLE)) {
                            worstCase = WorkStatus.AVAILABLE;
                            break AcquireWorstCase;
                        }
                    }
                    for (SortedSet<ScheduleElement> subrow : row.getSubRows().values()) {
                        monthRow.clear();
                        subrow.parallelStream()
                              .filter(scheduleElement -> monthInterval.overlaps(scheduleElement.getInterval()))
                              .forEach(monthRow::add);
                        for (ScheduleElement scheduleElement : monthRow) {
                            if (scheduleElement.getStatus() == WorkStatus.AVAILABLE) {
                                continue;
                            } else if (scheduleElement.getStatus() == WorkStatus.BLOCKED) {
                                worstCase = WorkStatus.BLOCKED;
                                break AcquireWorstCase;
                            } else if (scheduleElement.getStatus().compareTo(worstCase) < 0) {
                                worstCase = scheduleElement.getStatus();
                            }
                        }
                    }
                }

                outRow.add(new ScheduleElement(1, worstCase, ScheduleElement.WorkloadState.LESS, 0,
                                               new LinkedList<>(projects), monthInterval, -1));

            }
            ret.add(new ScheduleRow(row.getEmployee(), outRow, new HashMap<>()));
        }

        return new Tuple<>(ret, months);
    }

    @Override
    public PdfController getPdfController() {
        return pdfController;
    }

    private void assembleEqualElements(List<ScheduleElement> elements, ScheduleElement a, ScheduleElement b,
                                       Employee employee) {
        elements.remove(a);
        elements.remove(b);

        // Assemble and add overlapping element
        Integer weekSpan = a.getInterval().toPeriod() / DAYS_IN_WEEK;
        WorkStatus status = null;
        Integer workload = a.getWorkload() + b.getWorkload();
        ScheduleElement.WorkloadState load = ScheduleElement.WorkloadState.ofData(workload, employee.getWorkSchedule());
        List<Project> projects = new LinkedList<>(a.getProjects());
        projects.addAll(b.getProjects());
        elements.add(0, new ScheduleElement(weekSpan, status, load, workload, projects, a.getInterval(),
                                            a.getWorksOnId()));

    }

    private void separateEnclosingElements(List<ScheduleElement> elements, ScheduleElement enclosing,
                                           ScheduleElement enclosed, Employee employee) {
        elements.remove(enclosing);
        elements.remove(enclosed);
        LocalDateInterval enclosingInterval = enclosing.getInterval();
        LocalDateInterval enclosedInterval = enclosed.getInterval();
        // Create new intervals
        LocalDateInterval firstInterval = new LocalDateInterval(enclosingInterval.getStart(),
                                                                enclosedInterval.getStart());
        LocalDateInterval secondInterval = new LocalDateInterval(enclosedInterval.getEnd(),
                                                                 enclosingInterval.getEnd());
        LocalDateInterval overlapInterval = LocalDateInterval.getOverlap(enclosingInterval, enclosedInterval);

        // Create new elements
        ScheduleElement first = ScheduleElement.ofDifferentInterval(enclosing, firstInterval);
        ScheduleElement second = ScheduleElement.ofDifferentInterval(enclosing, secondInterval);

        // Assemble overlapping element
        if (!(overlapInterval == null)) {
            Integer weekSpan = overlapInterval.toPeriod() / DAYS_IN_WEEK;
            WorkStatus status = null;
            Integer workload = enclosing.getWorkload() + enclosed.getWorkload();
            ScheduleElement.WorkloadState load = ScheduleElement.WorkloadState
                    .ofData(workload, employee.getWorkSchedule());
            List<Project> projects = new LinkedList<>(enclosing.getProjects());
            projects.addAll(enclosed.getProjects());
            ScheduleElement overlap = new ScheduleElement(weekSpan, status, load, workload, projects,
                                                          overlapInterval, 0);

            // Add new elements to list
            elements.add(0, second);
            elements.add(0, overlap);
            elements.add(0, first);
        }
    }

    private void separateSimpleOverlappingElements(List<ScheduleElement> elements, ScheduleElement a, ScheduleElement b,
                                                   Employee employee) {
        elements.remove(a);
        elements.remove(b);
        LocalDateInterval aInterval = a.getInterval();
        LocalDateInterval bInterval = b.getInterval();
        LocalDateInterval firstInterval = aInterval.getDifference(bInterval);
        LocalDateInterval overlapInterval = LocalDateInterval.getOverlap(aInterval, bInterval);
        LocalDateInterval secondInterval = bInterval.getDifference(a.getInterval());
        ScheduleElement first = ScheduleElement.ofDifferentInterval(a, firstInterval);
        ScheduleElement second = ScheduleElement.ofDifferentInterval(b, secondInterval);

        // Assemble overlap element
        Integer weekSpan = overlapInterval.toPeriod() / DAYS_IN_WEEK;
        WorkStatus status = null;
        Integer workload = a.getWorkload() + b.getWorkload();
        ScheduleElement.WorkloadState load = ScheduleElement.WorkloadState
                .ofData(workload, employee.getWorkSchedule());
        List<Project> projects = new LinkedList<>(a.getProjects());
        projects.addAll(b.getProjects());
        ScheduleElement overlap = new ScheduleElement(weekSpan, status, load, workload, projects,
                                                      overlapInterval, 0);

        // Add separated elements back to list
        elements.add(0, second);
        elements.add(0, overlap);
        elements.add(0, first);

    }

    /**
     * fills gaps in a row of ScheduleElements.
     *
     * @param row      the element list in which it should add gaps
     * @param interval the expandedInterval in which it should add gaps
     */
    private void fillGaps(SortedSet<ScheduleElement> row, LocalDateInterval interval) {
        SortedSet<ScheduleElement> rowCopy = new TreeSet<>(row);
        Iterator rowIterator = row.iterator();
        ScheduleElement pre;

        if (rowIterator.hasNext()) {
            pre = (ScheduleElement) rowIterator.next();

            // Fill gap at the beginning
            if (interval.getStart().compareTo(pre.getInterval().getStart()) < 0) {
                LocalDateInterval gapInterval = new LocalDateInterval(interval.getStart().with(DayOfWeek.MONDAY),
                                                                      pre.getInterval().getStart().minusDays(1)
                                                                         .with(DayOfWeek.SUNDAY));
                rowCopy.add(new ScheduleElement(gapInterval.toPeriod() / DAYS_IN_WEEK,
                                                WorkStatus.AVAILABLE,
                                                ScheduleElement.WorkloadState.LESS,
                                                0,
                                                new LinkedList<>(),
                                                gapInterval,
                                                -1));
            }

            // Fill gaps
            while (rowIterator.hasNext()) {
                ScheduleElement cur = (ScheduleElement) rowIterator.next();
                if (pre.getInterval().getStart().compareTo(pre.getInterval().getEnd().plusDays(1)) < 0) {
                    LocalDateInterval gapInterval = new LocalDateInterval(
                            pre.getInterval().getEnd().plusDays(1).with(DayOfWeek.MONDAY),
                            cur.getInterval().getStart().minusDays(1).with(DayOfWeek.SUNDAY));
                    rowCopy.add(new ScheduleElement(gapInterval.toPeriod() / DAYS_IN_WEEK, WorkStatus.AVAILABLE,
                                                    ScheduleElement.WorkloadState.LESS, 0, new LinkedList<>(),
                                                    gapInterval,
                                                    -1));
                }
                pre = cur;
            }

            // Fill gap at the end
            if (pre.getInterval().getEnd().compareTo(interval.getEnd()) < 0) {
                LocalDateInterval gapInterval = new LocalDateInterval(
                        pre.getInterval().getEnd().plusDays(1).with(DayOfWeek.MONDAY),
                        interval.getEnd().with(DayOfWeek.SUNDAY));
                rowCopy.add(new ScheduleElement(gapInterval.toPeriod() / DAYS_IN_WEEK, WorkStatus.AVAILABLE,
                                                ScheduleElement.WorkloadState.LESS, 0, new LinkedList<>(), gapInterval,
                                                -1));
            }
        } else { // empty row
            rowCopy.add(new ScheduleElement(interval.toPeriod() / DAYS_IN_WEEK, WorkStatus.AVAILABLE,
                                            ScheduleElement.WorkloadState.LESS, 0, new LinkedList<>(), interval, -1));
        }

        row.clear();
        row.addAll(rowCopy);


    }

}
