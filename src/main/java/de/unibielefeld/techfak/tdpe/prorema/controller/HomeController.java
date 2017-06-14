package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ScheduleService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.WorksOnService;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.util.ArrayUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

/**
 * Controller for the homepage.
 */
@Controller
@Log4j2
public class HomeController extends ErrorHandler {

    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final WorksOnService worksOnService;
    private final ScheduleService scheduleService;

    @Autowired
    HomeController(ProjectService projectService, EmployeeService employeeService, WorksOnService worksOnService, ScheduleService scheduleService) {
        this.projectService = projectService;
        this.employeeService = employeeService;
        this.worksOnService = worksOnService;
        this.scheduleService = scheduleService;
    }

    @RequestMapping("/")
    public String base(Model model) {
        return home(model);
    }

    @RequestMapping("/home")
    public String home(Model model) {
        String home = "home";
        try {
            Employee loggedIn = LoginInfo.getCurrentLogin();
            if (LoginInfo.getPosition() == Employee.Position.ADMINISTRATOR ) {
                return adminhome(model);
            }
            String str = loggedIn.getFirstName();
            List<Project> curProjects = this.currentProjects(this.projectService.getByOrganisationUnit(loggedIn.getOrganisationUnitId()));
            calcEmployeeStats(loggedIn.getOrganisationUnitId(), model);

            model.addAttribute("projects", curProjects);
            model.addAttribute("homeName", str);
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing home.", e);
            throw e;
        }
        return home;
    }

    @RequestMapping("/impressum")
    public String impressum(Model model) {
        return "impressum";
    }

    @RequestMapping("/adminhome")
    public String adminhome(Model model) {
        Employee loggedIn = LoginInfo.getCurrentLogin();
        String str = loggedIn.getFirstName();
        model.addAttribute("homeName", str);
        model.addAttribute("position", LoginInfo.getPosition().toString());
        return "adminhome";
    }

    @RequestMapping("/impressumLoggedIn")
    public String impressumLogin(Model model) {

        model.addAttribute("position", LoginInfo.getPosition().toString());
        return "impressumLoggedIn";
    }

    @RequestMapping("/support")
    public String support(Model model) {

        model.addAttribute("listFragment", "supportList");
        model.addAttribute("gridFragment", "supportGrid");
        model.addAttribute("domain", "support");

        List<Employee> employees = employeeService.getByPosition(Employee.Position.ADMINISTRATOR.toString().toLowerCase());
        model.addAttribute("employees", employees);

        model.addAttribute("position", LoginInfo.getPosition().toString());

        return "list";
    }


    private void calcEmployeeStats(int unitId, Model model) {

        List<Employee> employees = employeeService.getByOrganisationUnit(unitId);

        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());

        int weeks = 0;
        ArrayList<String> weekDatesStrings = new ArrayList<>();
        for (LocalDate dateIterator = start; dateIterator.compareTo(
                end) <= 0; dateIterator = dateIterator.plusWeeks(1)) {
            weeks++;
        }
        
        String[] names = new String[employees.size()];
        int[] overBooked = new int[employees.size()];
        Integer[] planned = new Integer[employees.size()];
        int[] capacity = new int[employees.size()];

        int maxValue = 100;

        int counter = 0;
        for (Employee employee : employees) {

            int workloadPrecent = 0;
            int workSchedule = employee.getWorkSchedule();
            List<WorksOn> worksOns = worksOnService.getByDateAndEmployee(start, end, employee.getId());
            worksOns.removeAll(worksOnService.getByStatus(Project.Status.PROPOSAL.toString().toLowerCase()));

            int workload = 0;
            for (WorksOn worksOn : worksOns) {
                LocalDate toCheck;
                LocalDate endAnchor = worksOn.getEndDate().with(DayOfWeek.SUNDAY);
                if (worksOn.getStartDate().compareTo(start) < 0) {
                    toCheck = start; //if worksOn-startDate is already passed, start checkup at startDate
                } else {
                    toCheck = worksOn.getStartDate().with(DayOfWeek.MONDAY);
                }

                LocalDate weekIterator = start;
                for (int i = 0; i < weeks; i++) { //Iterate timespan weeks
                    weekIterator = start.plusWeeks(i);
                    if (weekIterator.compareTo(toCheck) >= 0 && weekIterator.compareTo(
                            endAnchor) <= 0) { //Add block to array
                        workload = workload + worksOn.getWorkload();
                    }
                }
            }

            workloadPrecent = (workload * 100 / (workSchedule * weeks));

            if (workloadPrecent > maxValue) {
                maxValue = workloadPrecent;
            }

            int regular = 100;
            int irregular = 0;
            if (workloadPrecent > 100) {
                irregular = workloadPrecent - 100;
            } else {
                regular = workloadPrecent;
            }

            names[counter] = employee.getFirstName() + ' ' + employee.getLastName();
            planned[counter] = regular;
            overBooked[counter] = irregular;
            capacity[counter] = 100 - workloadPrecent;

            counter++;
        }

        model.addAttribute("maxValue", maxValue);
        model.addAttribute("names", names);
        model.addAttribute("planned", planned);
        model.addAttribute("irregular", overBooked);
        model.addAttribute("capacity", capacity);


        scheduleService.getStatistics(model, LoginInfo.getCurrentLogin(), start, end, weeks, unitId);
    }

    /**
     * Select the 3 most recent projects
     * @param projectList List<Project>
     * @return List<Project>
     */
    private List<Project> currentProjects(List<Project> projectList) {

        for (Iterator<Project> iter = projectList.iterator(); iter.hasNext(); ) {
            Project pro = iter.next();
            if (!pro.getRunning() || pro.getEndDate() == null) {
                iter.remove();
            }
        }

        // select the 4 most most recent projects
        while (projectList.size() > 3) {
            int remProject = 0;
            for (Project pro:projectList) {
                if (pro.getStartDate() != null) {
                    if (pro.getStartDate().isAfter(projectList.get(remProject).getStartDate())) {
                        remProject = projectList.indexOf(pro);
                    }
                } else {
                    remProject = projectList.indexOf(pro);
                    break;
                }
            }
            projectList.remove(remProject);
        }

        //compute remaing Time for every Project
        for (Project pro: projectList) {
            LocalDate currentDate = LocalDate.now();

            int div = (int) (pro.getEndDate().toEpochDay() - currentDate.toEpochDay());

            int month = 0;

            while (div > currentDate.lengthOfMonth()) {
                div = div - currentDate.lengthOfMonth();
                month++;
                currentDate.plusDays(currentDate.lengthOfMonth());
            }

            int weeks = div / 7;
            int days = div % 7;

            String strMonth = " Monate ";
            String strWeek = " Wochen ";
            String strDay = " Tage ";

            if (month == 1) {
                strMonth = " Monat ";
            }
            if (weeks == 1) {
                strWeek = " Woche ";
            }
            if (days == 1) {
                strDay = " Tag ";
            }

            String remainingTime = month + strMonth + weeks + strWeek + days + strDay;
            pro.setRemainingTime(remainingTime);
        }

        // Liste wird nach Datum sortiert
        for (int i = 1; i < projectList.size(); i++) {
            for (int j = 0; j < projectList.size() - i; j++) {
                if (projectList.get(j).getEndDate().toEpochDay() > projectList.get(j + 1).getEndDate().toEpochDay()) {
                    Project tmp = projectList.get(j);
                    projectList.set(j, projectList.get(j + 1));
                    projectList.set(j + 1, tmp);
                }
            }
        }
        return projectList;
    }
}
