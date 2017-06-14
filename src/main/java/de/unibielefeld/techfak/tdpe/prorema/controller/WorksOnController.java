package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewWorksOnCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.*;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.*;
import de.unibielefeld.techfak.tdpe.prorema.locking.WorksOnLock;
import de.unibielefeld.techfak.tdpe.prorema.locking.WorksOnLockInterceptor;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.utils.SkillHelper;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * This is the controller responsible for the WorksOnForm.
 * Created by Frederik on 20.05.2016.
 */
@Controller
@Log4j2
public class WorksOnController extends ErrorHandler {

    private WorksOnService worksOnService;
    private ProjectService projectService;
    private EmployeeService employeeService;
    private NeedsSkillService needsSkillService;
    private ClientService clientService;
    private OrganisationUnitService unitService;
    private SkillHelper skillHelper;

    private static final int FULLY_COVERED_MULTIPLIER = 3;
    private static final int LOW_COVERED_MULTIPLIER = 5;
    private static final int NOT_COVERED_MULTIPLIER = 3;

    private static final int FULLY_COVERED = 25;
    private static final int LOW_COVERED = 55;
    private static final int NOT_COVERED = 94;

    @Autowired
    public WorksOnController(WorksOnService worksOnService, ProjectService projectService,
                             EmployeeService employeeService, ClientService clientService,
                             OrganisationUnitService unitService, NeedsSkillService needsSkillService,
                             SkillHelper skillHelper) {
        this.worksOnService = worksOnService;
        this.projectService = projectService;
        this.employeeService = employeeService;
        this.clientService = clientService;
        this.unitService = unitService;
        this.needsSkillService = needsSkillService;
        this.skillHelper = skillHelper;
    }

    /**
     * This method creates the necessary Cmd and shows the form.
     *
     * @param newWorksOnCmd the new WorksOn to be created
     * @param model         the model to add things to
     * @param worksOnId     the worksOnId for editing
     * @param prjId         the id for preload
     * @param emplId        the employeeId for preload
     * @param startDate     the startDate for creating from the schedule
     * @param unitId        the unitId of the project or employee to about requesting
     * @return
     */
    @RequestMapping(value = "/worksOnForm", method = RequestMethod.GET)
    public String worksOnForm(NewWorksOnCmd newWorksOnCmd, Model model,
                              @RequestParam(value = "worksOnId", required = false) Integer worksOnId,
                              @RequestParam(value = "prjId", required = false) Integer prjId,
                              @RequestParam(value = "emplId", required = false) Integer emplId,
                              @RequestParam(value = "startDate", required = false) String startDate,
                              @RequestParam(value = "unitId", required = false) Integer unitId) {

        model.addAttribute("orgaUnitId", LoginInfo.getCurrentLogin().getOrganisationUnitId());
        try {
            if (newWorksOnCmd == null) {
                newWorksOnCmd = new NewWorksOnCmd();
            }

            WorksOnDetails newWorkDetails = newWorksOnCmd.getWorkDetails().get(0);
            newWorkDetails.setWorkload(40);
            if (emplId != null) {
                newWorksOnCmd.getEmployeeIds().add(emplId);
            }
            if (startDate != null) {
                newWorkDetails.setStartDate(startDate);
            }

            if (prjId != null) {
                newWorksOnCmd.setProjectId(prjId);
                Project project = projectService.findOne(prjId);
                if (project != null) {
                    LocalDate projectStartDate = project.getStartDate();
                    if (projectStartDate != null) {
                        newWorkDetails.setStartDate(projectStartDate.toString());
                    }
                    LocalDate projectEndDate = project.getEndDate();
                    if (projectEndDate != null) {
                        newWorkDetails.setEndDate(projectEndDate.toString());
                    }
                }
            }

            calculateRecommendations(model, prjId);

            if (worksOnId != null) {
                WorksOn worksOn = worksOnService.findOne(worksOnId);
                newWorksOnCmd = new NewWorksOnCmd(worksOn);
            }
            if (unitId != null) {
                checkPermission(unitId, newWorksOnCmd);
                newWorksOnCmd.setUnitId(unitId);
            } else {
                newWorksOnCmd.setPermission(Boolean.TRUE);
            }
            addAttributes(model, newWorksOnCmd);
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing worksOnForm.", e);
            throw e;
        }

        return "worksOnForm";
    }

    private void calculateRecommendations(Model model, Integer prjId) {
        Map<Integer, Integer> recommendation = new HashMap<>();
        Map<Integer, Skill.SkillLevel> notCoveredSkills = new HashMap<>();
        Map<Integer, Skill.SkillLevel> lowCoveredSkills = new HashMap<>();
        Map<Integer, Skill.SkillLevel> fullyCoveredSkills = new HashMap<>();
        model.addAttribute("recommendation", recommendation);
        model.addAttribute("notCoveredSkills", notCoveredSkills);
        model.addAttribute("lowCoveredSkills", lowCoveredSkills);
        model.addAttribute("fullyCoveredSkills", fullyCoveredSkills);
        if (prjId != null) {
            List<Employee> employees = employeeService.getAllNonExitedNonAdmin();
            model.addAttribute("employees", employees);
            Map<String, List<Employee>> organisationUnitEmployeeMap = new TreeMap<>();
            employees.forEach(employee -> {
                String organisationUnit = unitService.findOne(employee.getOrganisationUnitId()).getName();
                if (!organisationUnitEmployeeMap.containsKey(organisationUnit)) {
                    organisationUnitEmployeeMap.put(organisationUnit, new LinkedList<>());
                }
                organisationUnitEmployeeMap.get(organisationUnit).add(employee);
            });
            model.addAttribute("organisationUnitEmployee", organisationUnitEmployeeMap);

            List<Employee> employeesOnProject = fetchProjectEmployees(prjId);

            Map<Integer, Tuple<Integer, Skill.SkillLevel>> neededSkillMap = skillHelper.getNeedsSkillList(prjId, employeesOnProject);

            //Put skills into corresponding Lists
            neededSkillMap.forEach((skillId, tuple) -> {
                if (tuple.getLeft() <= 0) {
                    fullyCoveredSkills.put(skillId, tuple.getRight());
                } else if (tuple.getLeft() < 3) {
                    lowCoveredSkills.put(skillId, tuple.getRight());
                } else {
                    notCoveredSkills.put(skillId, tuple.getRight());
                }
            });

            employees.forEach(employee -> {
                int employeeId = employee.getId();
                employee.getSkillList().forEach(skillTuple -> {
                    int skillId = skillTuple.getLeft().getId();
                    int enhancement = 0;

                    if (fullyCoveredSkills.containsKey(skillId)) {

                        int match = fullyCoveredSkills.get(skillId).matchesWith(skillTuple.getRight());
                        enhancement = FULLY_COVERED - (match * FULLY_COVERED_MULTIPLIER);

                    } else if (lowCoveredSkills.containsKey(skillId)) {

                        int match = lowCoveredSkills.get(skillId).matchesWith(skillTuple.getRight());
                            enhancement = LOW_COVERED - (match * LOW_COVERED_MULTIPLIER);

                    } else if (notCoveredSkills.containsKey(skillId)) {

                        int match = notCoveredSkills.get(skillId).matchesWith(skillTuple.getRight());
                        enhancement = NOT_COVERED - (match * NOT_COVERED_MULTIPLIER);
                    }

                    if (recommendation.containsKey(employeeId)) {
                        recommendation.replace(employeeId, recommendation.get(employeeId) + enhancement);
                    } else {
                        recommendation.put(employeeId, enhancement);
                    }
                });
            });
        }
    }

    private List<Employee> fetchProjectEmployees(Integer prjId) {
        // Load employees in this project
        List<Employee> employees = new ArrayList<>();
        HashSet<Integer> uniqueEmIds = new HashSet<>();

        List<WorksOn> worksOns = worksOnService.getWorksOnByProject(prjId);

        for (WorksOn worksOn : worksOns) {
            int emId = worksOn.getEmployeeId();
            uniqueEmIds.add(emId);
        }
        uniqueEmIds.forEach(emId -> {
            employees.add(employeeService.findOne(emId));
        });
        return employees;
    }

    /**
     * This method adds more workson rows to the form
     * @param newWorksOnCmd
     * @param model
     * @return
     */
    @RequestMapping(value = "/worksOnForm", params = { "addDetails" }, method = RequestMethod.POST)
    public String addRow(NewWorksOnCmd newWorksOnCmd, Model model) {
        WorksOnDetails worksOnDetails = new WorksOnDetails();
        worksOnDetails.setId(newWorksOnCmd.getWorkDetails().size());
        WorksOnDetails first = newWorksOnCmd.getWorkDetails().get(0);
        String start = first.getEndDate();
        worksOnDetails.setStartDate(start);
        worksOnDetails.setEndDate(start);
        worksOnDetails.setWorkload(first.getWorkload());
        newWorksOnCmd.getWorkDetails().add(worksOnDetails);

        calculateRecommendations(model, newWorksOnCmd.getProjectId());

        addAttributes(model, newWorksOnCmd, true);
        return "worksOnForm";
    }


    /**
     * This method removes an unwanted details-row from the form
     * @param newWorksOnCmd
     * @param req
     * @param model
     * @return
     */
    @RequestMapping(value = "/worksOnForm", params = {"removeDetails"}, method = RequestMethod.POST)
    public String removeRow(NewWorksOnCmd newWorksOnCmd, final HttpServletRequest req, Model model) {
        final Long detailsId = Long.valueOf(req.getParameter("removeDetails"));

        List<WorksOnDetails> workDetails = newWorksOnCmd.getWorkDetails();
        for (WorksOnDetails details : workDetails) {
            if (details.getId() == detailsId) {
                newWorksOnCmd.getWorkDetails().remove(details);
                break;
            }
        }
        //check that no ids are given twice and all are in line
        workDetails = newWorksOnCmd.getWorkDetails();
        int id = 0;
        for(WorksOnDetails details : workDetails) {
            if (details.getId() == id) {
                id++;
            } else {
                details.setId(id);
                id++;
            }
        }

        calculateRecommendations(model, newWorksOnCmd.getProjectId());

        addAttributes(model, newWorksOnCmd);
        return "worksOnForm";
    }


    /**
     * Processing of a worksOn form.
     *
     * @param newWorksOnCmd command
     * @param bindingResult binding result
     * @param model         model
     * @return skillform if invalid, redirect to skilllist if successive
     */
    @RequestMapping(value = "/worksOnForm", params = {"action"}, method = RequestMethod.POST)
    public String saveWorksOnForm(/*@Valid*/ NewWorksOnCmd newWorksOnCmd, BindingResult bindingResult, Model model,
                                  @RequestParam(value = "action", required = true) String action) {

        if (action.equals("save")) {
            return saveWorksOn(newWorksOnCmd, bindingResult, model);
        } else if (action.equals("delete")) {
            return deleteWorksOn(newWorksOnCmd, model);
        } else if (action.equals("ask")) {
            return askForEmployee(newWorksOnCmd, bindingResult, model);
        } else {
            newWorksOnCmd.setUnitId(updateUnitId(newWorksOnCmd));
            addAttributes(model, newWorksOnCmd);
            model.addAttribute("position", LoginInfo.getPosition().toString());
            return "worksOnForm";
        }
    }

    private String saveWorksOn(NewWorksOnCmd newWorksOnCmd, BindingResult bindingResult, Model model) {
        try {
            model.addAttribute("overbooked", 0);
            if (bindingResult.hasErrors()) {
                newWorksOnCmd.setUnitId(updateUnitId(newWorksOnCmd));
                addAttributes(model, newWorksOnCmd);
                model.addAttribute("position", LoginInfo.getPosition().toString());
                return "worksOnForm";
            } else {
                for(WorksOnDetails workDetails : newWorksOnCmd.getWorkDetails()) {
                    if (newWorksOnCmd.getSaveOverbooked() != null && newWorksOnCmd.getSaveOverbooked() == 1) {
                        worksOnService.createWorksOn(newWorksOnCmd);
                        return redirect(newWorksOnCmd.getLastUrl());
                    }

                    List<Integer> overbookedIds;
                    int worksOnId = -1;
                    if (newWorksOnCmd.getId() != null) {
                        worksOnId = newWorksOnCmd.getId();
                    }

                    if ((overbookedIds = checkIfOverbooked(newWorksOnCmd.getEmployeeIds(), model, workDetails, worksOnId)).size() > 0) {

                        //Remove overbooked employees to save the others first, then
                        // remove the remaining and re-add the overbooked ones
                        newWorksOnCmd.removeEmployeeIds(overbookedIds);
                        if (newWorksOnCmd.getEmployeeIds().size() > 0) {
                            worksOnService.createWorksOn(newWorksOnCmd);
                        }

                        newWorksOnCmd.removeEmployeeIds(newWorksOnCmd.getEmployeeIds());
                        newWorksOnCmd.addEmployeeIds(overbookedIds);

                        addAttributes(model, newWorksOnCmd);
                        model.addAttribute("overbooked", 1);
                        log.info("Overbooked employee(s) found!");
                        return "worksOnForm";
                    }
                }
                worksOnService.createWorksOn(newWorksOnCmd);
                return redirect(newWorksOnCmd.getLastUrl());
            }
        } catch (Exception e) {
            log.error("Error evaluating WorksOnForm.");
            throw e;
        }
    }

    private String deleteWorksOn(NewWorksOnCmd newWorksOnCmd, Model model) {
        try {
            worksOnService.delete(newWorksOnCmd.getId());
            return redirect(newWorksOnCmd.getLastUrl());
        }catch (Exception e) {
            log.error("Error deleting WorksOn entry.");
            newWorksOnCmd.setUnitId(updateUnitId(newWorksOnCmd));
            addAttributes(model, newWorksOnCmd);
            model.addAttribute("position", LoginInfo.getPosition().toString());
            return "worksOnForm";
        }
    }

    private String askForEmployee(NewWorksOnCmd newWorksOnCmd, BindingResult bindingResult, Model model) {
        try {
            if (bindingResult.hasErrors()) {
                newWorksOnCmd.setUnitId(updateUnitId(newWorksOnCmd));
                addAttributes(model, newWorksOnCmd);
                model.addAttribute("position", LoginInfo.getPosition().toString());
                log.warn("Employeerequest form BindingResult has errors!");
                return "worksOnForm";
            } else {
                worksOnService.createWorksOn(newWorksOnCmd);
                return redirect(newWorksOnCmd.getLastUrl());
            }
        } catch (Exception e) {
            log.error("Error evaluating WorksOnForm.");
            throw e;
        }
    }

    /**
     * A method to load the necessary models for the form.
     */
    private void addAttributes(Model model, NewWorksOnCmd newWorksOnCmd) {
        addAttributes(model, newWorksOnCmd, false);
    }

    /**
     * A method to load the necessary models for the form.
     */
    private void addAttributes(Model model, NewWorksOnCmd newWorksOnCmd, boolean excludeAbsence) {
        ArrayList<WorksOn.WorkStatus> statusList = new ArrayList<>(Arrays.asList(WorksOn.WorkStatus.values()));
        statusList.remove(WorksOn.WorkStatus.AVAILABLE);
        statusList.remove(WorksOn.WorkStatus.NOT_SPECIFIED);
        if(excludeAbsence){
            statusList.remove(WorksOn.WorkStatus.ABSENCE);
        }
        List<Employee> employees = employeeService.getAllNonExitedNonAdmin();
        model.addAttribute("states", statusList);
        Integer unitId = newWorksOnCmd.getUnitId();
        model.addAttribute("responsible", findResponsible(unitId));
        model.addAttribute("loginInfo", LoginInfo.getCurrentLogin());
        model.addAttribute("projects", projectService.getAll());
        model.addAttribute("employees", employees);
        model.addAttribute("clients", clientService.getAll());
        model.addAttribute("newWorksOnCmd", newWorksOnCmd);
        Employee test = LoginInfo.getCurrentLogin();
        model.addAttribute("orgaUnitId", LoginInfo.getCurrentLogin().getOrganisationUnitId());
        Map<String, List<Employee>> organisationUnitEmployeeMap = new TreeMap<>();
        employees.forEach(employee -> {
            String organisationUnit = unitService.findOne(employee.getOrganisationUnitId()).getName();
            if (!organisationUnitEmployeeMap.containsKey(organisationUnit)) {
                organisationUnitEmployeeMap.put(organisationUnit, new LinkedList<>());
            }
            organisationUnitEmployeeMap.get(organisationUnit).add(employee);
        });
        model.addAttribute("organisationUnitEmployee", organisationUnitEmployeeMap);
    }

    private void checkPermission(Integer unitId, NewWorksOnCmd newWorksOnCmd) {
        if (unitId != null) {
            Integer userUnit = LoginInfo.getCurrentLogin().getOrganisationUnitId();
            if (userUnit != null) {
                Boolean permission = (unitId.equals(userUnit));
                newWorksOnCmd.setPermission(permission);
            } else {
                newWorksOnCmd.setPermission(Boolean.FALSE);
            }


        } else {
            Boolean permission = Boolean.TRUE;
            newWorksOnCmd.setPermission(permission);
        }

    }

    private String redirect(String lastUrl) {
        String url;

        url = lastUrl;
        url = url.substring(url.lastIndexOf("/"));
        if (url.contains("login") || url.contains("worksOnForm")) { //check whether the user would be an ill-redirected
            url = "/schedule";
        }
        return "redirect:" + url;
    }

    private Employee findResponsible(Integer unitId) {
        if (unitId != null) {
            OrganisationUnit unit = unitService.findOne(unitId);
            Integer respId = unit.getFirstManagerId();
            Employee resp = employeeService.findOne(respId);
            return resp;
        } else {
            Employee resp = employeeService.findOne(0);
            return resp;
        }

    }

    private List<Integer> checkIfOverbooked(List<Integer> employeeIds, Model model, WorksOnDetails workDetails, int worksOnId) {

        LocalDate start = LocalDate.parse(workDetails.getStartDate());
        LocalDate end = LocalDate.parse(workDetails.getEndDate());
        int additionalWorkload = workDetails.getWorkload();
        List<Integer> idList = new ArrayList<>(employeeIds);

        int weeks = 0;
        for (LocalDate dateIterator = start; dateIterator
                .compareTo(end) <= 0; dateIterator = dateIterator
                .plusWeeks(1)) {
            weeks++;
        }

        Map<Employee, List<Integer>> emOverBookedBy = new HashMap<>();

        for (int employeeId : employeeIds) {

            Employee employee = employeeService.findOne(employeeId);
            emOverBookedBy.put(employee, new ArrayList<>());

            List<WorksOn> worksOns = worksOnService.getByDateAndEmployee(start, end, employeeId);
            worksOns.removeAll(worksOnService.getByStatus(Project.Status.PROPOSAL.toString().toLowerCase()));

            int worksOnTotal = additionalWorkload * weeks;

            for (WorksOn worksOn : worksOns) {

                if (worksOnId != worksOn.getId()) {
                    int prjId = worksOn.getProjectId();
                    emOverBookedBy.get(employee).add(prjId);

                    LocalDate toCheck;
                    LocalDate endAnchor = worksOn.getEndDate().with(DayOfWeek.MONDAY);
                    if (worksOn.getStartDate().compareTo(start) < 0) {
                        toCheck = start; //if worksOn-startDate is already passed, start checkup at startDate
                    } else {
                        toCheck = worksOn.getStartDate().with(DayOfWeek.MONDAY);
                    }

                    LocalDate weekIterator = toCheck;
                    for (int counter = 0; counter < weeks; counter++) {//Iterate timespan weeks
                        weekIterator = start.plusWeeks(counter);
                        if (weekIterator.compareTo(toCheck) >= 0 && weekIterator.compareTo(
                                endAnchor) <= 0) {//Add block to array
                            worksOnTotal = worksOnTotal + worksOn.getWorkload();
                        }
                    }
                }
            }

            if (worksOnTotal <= employee.getWorkSchedule() * weeks) {
                emOverBookedBy.remove(employee);
                idList.remove((Integer) employeeId);
            }
        }

        if (!emOverBookedBy.isEmpty()) {
            List<ProjectsAndManagerMails> emProjectsAndManagerMails = new ArrayList<>();
            emOverBookedBy.forEach((employee, prjIds) -> {
                Map<Project, String> projectMailMap = new HashMap<Project, String>();
                prjIds.forEach(id -> {
                    Project project = projectService.findOne(id);
                    projectMailMap.put(project, employeeService.findOne(project.getProjectManagerId()).getMail());
                });
                emProjectsAndManagerMails.add(new ProjectsAndManagerMails(employee.getFirstName()+' '+employee.getLastName(), projectMailMap));
            });

            model.addAttribute("projectsAndManagerMails", emProjectsAndManagerMails);
        }
        return idList;
    }

    private Integer updateUnitId(NewWorksOnCmd newWorksOnCmd) {
        Integer id = newWorksOnCmd.getEmployeeIds().get(0);
        Employee empl;
        if (id != null) {
            empl = employeeService.findOne(id);
        } else{
            empl = employeeService.findOne(0);
        }
        return empl.getOrganisationUnitId();
    }


    /**
     * Returns feedback about the lock.
     *
     * @param model autowired
     * @return lock feedback
     */
    @RequestMapping(value = "/worksOnLocked")
    String resourceIsLocked(Model model) {
        try {
            WorksOnLock lock = (WorksOnLock) model.asMap().get(WorksOnLockInterceptor.LOCK_ATTRIBUTE_NAME);
            model.addAttribute("endDate", lock.getBeginDateTime().plus(lock.getDuration()));
            Employee holder = employeeService.findOne(lock.getHolderId());
            model.addAttribute("holder",
                    String.join(" ", holder.getNameTitle(), holder.getFirstName(), holder.getLastName()));
            model.asMap().remove(WorksOnLockInterceptor.LOCK_ATTRIBUTE_NAME);
        } catch (Exception e) {
            log.error("Error showing locking information.", e);
            throw e;
        }
        return "simpleLocked";
    }


}
