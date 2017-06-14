package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewProjectCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.*;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.*;
import de.unibielefeld.techfak.tdpe.prorema.locking.DomainIdentifier;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.utils.SkillHelper;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Created by Alex Schneider.
 */
@Controller
@Log4j2
public class ProjectController extends ErrorHandler {

    private static final String MANAGER = "managerMap";
    private static final String UNITS = "unitMap";
    private static final String CLIENTS = "clientMap";
    private static final String PROJECT = "project";
    private static final String PROJECTS = "projects";
    private static final String CONTACTS = "contactMap";
    private static final String LIST_FRAGMENT = "listFragment";
    private static final String GRID_FRAGMENT = "gridFragment";
    private static final String DOMAIN = "domain";
    private static final String LIST = "list";

    private final ProjectService projectService;
    private final ClientService clientService;
    private final ContactService contactService;
    private final OrganisationUnitService organisationUnitService;
    private final WorksOnService worksOnService;
    private final EmployeeService employeeService;
    private final SkillService skillService;
    private ProjectEntity pe;
    private final NeedsSkillService needsSkillService;
    private final SkillHelper skillHelper;
    private final SimpleLockService simpleLockService;

    @Autowired
    public ProjectController(ProjectService projectService, ClientService clientService, ContactService contactService,
                             OrganisationUnitService organisationUnitService, WorksOnService worksOnService,
                             EmployeeService employeeService, SkillService skillService,
                             NeedsSkillService needsSkillService, SkillHelper skillHelper,
                             SimpleLockService simpleLockService) {
        this.projectService = projectService;
        this.clientService = clientService;
        this.contactService = contactService;
        this.organisationUnitService = organisationUnitService;
        this.worksOnService = worksOnService;
        this.employeeService = employeeService;
        this.skillService = skillService;
        this.needsSkillService = needsSkillService;
        this.skillHelper = skillHelper;
        this.simpleLockService = simpleLockService;
    }

    @RequestMapping("/pipeline")
    public String pipeline(Model model) {
        try {
            // List specific attributes
            model.addAttribute(LIST_FRAGMENT, "pipelineList");
            model.addAttribute(GRID_FRAGMENT, "pipelineGrid");
            model.addAttribute(DOMAIN, "pipeline");

            //Domain/fragment specific attributes
            List<Project> projects = projectService.getAll();
            projects.removeAll(projectService.getByRunning(Boolean.TRUE));

            Map<Integer, Client> clients = new HashMap<>();
            Map<Integer, OrganisationUnit> units = new HashMap<>();
            projects.forEach(project1 -> {
                units.put(project1.getId(), organisationUnitService.findOne(project1.getOrgaUnitId()));
            });

            model.addAttribute(UNITS, units);
            model.addAttribute(CLIENTS, clients);
            model.addAttribute(PROJECTS, projects);
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing project list.", e);
            throw e;
        }
        return LIST;
    }

    @RequestMapping(value = "/singleproject", method = RequestMethod.GET)
    public String singleproject(@RequestParam(name = "id") int id, Model model) {
        try {
            Project project = projectService.findOne(id);

            if (project != null) {

                // Load employees in this project
                List<Employee> employees = new ArrayList<>();
                HashSet<Integer> uniqueEmIds = new HashSet<>();
                List<String> labels = new ArrayList<>();
                List<Integer> values = new ArrayList<>();

                int totalWorkload = 0;

                List<WorksOn> worksOns = worksOnService.getWorksOnByProject(id);

                for (WorksOn worksOn : worksOns) {
                    int emId = worksOn.getEmployeeId();
                    int workload = worksOn.getWorkload();

                    uniqueEmIds.add(emId);

                    LocalDate toCheck = worksOn.getStartDate().with(DayOfWeek.MONDAY);
                    LocalDate endAnchor = worksOn.getEndDate().with(DayOfWeek.MONDAY);

                    for (LocalDate weekIterator = toCheck; weekIterator.compareTo(
                            endAnchor) <= 0; weekIterator = weekIterator.plusWeeks(1)) {
                        if (weekIterator.compareTo(toCheck) >= 0 && weekIterator.compareTo(
                                endAnchor) <= 0) { //Add block to array
                            int workingDaysPerWeek = workload / 8;
                            totalWorkload += workingDaysPerWeek;
                            Employee em = employeeService.findOne(emId);
                            String emFullName = em.getFirstName() + ' ' + em.getLastName();
                            if (labels.contains(emFullName)) {
                                int index = labels.indexOf(emFullName);
                                values.set(index, values.get(index) + workingDaysPerWeek);
                            } else {
                                labels.add(emFullName);
                                values.add(workingDaysPerWeek);
                            }
                        }
                    }
                }
                uniqueEmIds.forEach(emId -> {
                    employees.add(employeeService.findOne(emId));
                });

                int coverage = totalWorkload;
                int rest;
                Integer menDays = project.getMenDays();
                if (menDays != null) {
                    if ((rest = menDays - coverage) > 0) {
                        labels.add(0, "Nicht abgedeckte Arbeitstage");
                        values.add(0, rest);
                    }
                }

                Map<Integer, Tuple<Integer, Skill.SkillLevel>> neededSkillMap = skillHelper
                        .getNeedsSkillList(id, employees);

                List<Tuple<Skill, Skill.SkillLevel>> notCoveredSkills = new ArrayList<>();
                List<Tuple<Skill, Skill.SkillLevel>> lowCoveredSkills = new ArrayList<>();
                List<Tuple<Skill, Skill.SkillLevel>> fullyCoveredSkills = new ArrayList<>();
                //Put skills into corresponding Lists
                neededSkillMap.forEach((skillId, tuple) -> {
                    if (tuple.getLeft() <= 0) {
                        fullyCoveredSkills.add(new Tuple<>(skillService.findOne(skillId), tuple.getRight()));
                    } else if (tuple.getLeft() < 3) {
                        lowCoveredSkills.add(new Tuple<>(skillService.findOne(skillId), tuple.getRight()));
                    } else {
                        notCoveredSkills.add(new Tuple<>(skillService.findOne(skillId), tuple.getRight()));
                    }
                });

                // Load contact if set
                Integer contactId = project.getContactId();
                Contact contact = contactId != null ? contactService.findOne(contactId) : null;

                // Load project manager if set
                Integer projectManagerId = project.getProjectManagerId();
                Employee projectManager = projectManagerId != null ? employeeService.findOne(projectManagerId) : null;

                // Load client if set
                Integer clientId = contact.getClientId();
                Client client = clientId != null ? clientService.findOne(clientId) : null;

                Integer orgaUnitId = project.getOrgaUnitId();
                OrganisationUnit orgaUnit = orgaUnitId != null ? organisationUnitService.findOne(orgaUnitId) : null;

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                model.addAttribute("project", project);
                model.addAttribute("contact", contact);
                model.addAttribute("client", client);
                model.addAttribute("notCoveredSkills", notCoveredSkills);
                model.addAttribute("lowCoveredSkills", lowCoveredSkills);
                model.addAttribute("fullyCoveredSkills", fullyCoveredSkills);
                model.addAttribute("orgaUnit", orgaUnit);
                model.addAttribute("projectManager", projectManager);
                String positionString = LoginInfo.getPosition().toString();
                if (!StringUtils.isEmpty(positionString)) {
                    model.addAttribute("position", positionString);
                }
                LocalDate startDate = project.getStartDate();
                if (startDate != null) {
                    model.addAttribute("startDate", startDate.format(dateTimeFormatter));
                }
                LocalDate endDate = project.getEndDate();
                if (endDate != null) {
                    model.addAttribute("endDate", endDate.format(dateTimeFormatter));
                }
                if (menDays != null && menDays != 0) {
                    model.addAttribute("percent", Math.round((10000 * coverage) / menDays) / 100.0);
                }
                model.addAttribute("coverage", coverage);
                model.addAttribute("labels", labels);
                model.addAttribute("values", values);
                model.addAttribute("teamMembers", employees);

                List<ChangelogEntry> changelogs = project.getHistory();
                Map<Integer, Employee> users = new HashMap<>();
                changelogs.forEach(changelogEntry -> {
                    users.put(changelogEntry.getChangelogEntryId(),
                              employeeService.findOne(changelogEntry.getUserId()));
                });
                model.addAttribute("changelogs", changelogs);
                model.addAttribute("users", users);

            } else {
                log.error("Project with ID " + id + " not found!");
                model.addAttribute("error", "Das Projekt mit der ID " + id + " wurde nicht gefunden!");
            }
        } catch (Exception e) {
            log.error("Error showing profile.", e);
            throw e;
        }
        return "singleproject";
    }

    @RequestMapping(value = "singleproject/delete", method = RequestMethod.GET)
    public String deleteProject(@RequestParam(name = "id") int projectId) {
        worksOnService.delete(projectId);
        projectService.delete(projectId);
        return "redirect:/projects";
    }

    @RequestMapping(value = "/projectform", method = RequestMethod.GET)
    public String projectform(NewProjectCmd newProjectCmd, Model model,
                              @RequestParam(value = "id", required = false) String id) {
        try {
            if (id != null) {
                Project project = projectService.findOne(Integer.valueOf(id));
                if (project != null) {

                    newProjectCmd.setId(id);
                    newProjectCmd.setName(project.getName());
                    newProjectCmd.setStatus(project.getStatus().name());
                    newProjectCmd.setDescription(project.getDescription());
                    LocalDate startDate = project.getStartDate();
                    if (startDate != null) {
                        newProjectCmd.setStartDate(startDate.toString());
                    }
                    LocalDate endDate = project.getEndDate();
                    if (endDate != null) {
                        newProjectCmd.setEndDate(endDate.toString());
                    }
                    Boolean running = project.getRunning();
                    if (running != null) {
                        newProjectCmd.setRunning(running.toString());
                    }
                    newProjectCmd.setMenDays(project.getMenDays());
                    newProjectCmd.setConversionProbability(project.getConversionProbability());
                    newProjectCmd.setPotentialProjectVolume(project.getPotentialProjectVolume());
                    newProjectCmd.setWeightedProjectVolume(project.getWeightedProjectVolume());
                    newProjectCmd.setCountry(project.getCountry());
                    newProjectCmd.setCity(project.getCity());
                    newProjectCmd.setStreet(project.getStreet());
                    newProjectCmd.setZip(project.getZip());
                    newProjectCmd.setOrgaUnitId(project.getOrgaUnitId().toString());

                    //Ersatz für die unten stehenden zwei if-Abfragen
                    Integer newProjectManagerId = project.getProjectManagerId();
                    if (newProjectManagerId != null) {
                        newProjectCmd.setProjectManagerId(newProjectManagerId.toString());
                    }
                    Integer newContactId = project.getContactId();
                    if (newContactId != null) {
                        newProjectCmd.setContactId(newContactId.toString());
                    }

                    model.addAttribute(newProjectCmd);

                    // Load required skills
                    List<NeedsSkill> needsSkillList = needsSkillService.getSkillList(Integer.valueOf(id));
                    model.addAttribute("needsSkilllist", needsSkillList);
                }
            }

        } catch (Exception e) {
            log.error("Error showing projectform.", e);
            throw e;
        }
        Map<String, List<Employee>> organisationUnitManagerMap = new TreeMap<>();
        employeeService.getAllExceptAdmins().forEach(employee -> {
            String organisationUnit = organisationUnitService.findOne(employee.getOrganisationUnitId()).getName();
            if (!organisationUnitManagerMap.containsKey(organisationUnit)) {
                organisationUnitManagerMap.put(organisationUnit, new LinkedList<>());
            }
            organisationUnitManagerMap.get(organisationUnit).add(employee);
        });
        model.addAttribute("organisationUnitManager", organisationUnitManagerMap);
        model.addAttribute("skills", skillService.getAll());
        model.addAttribute("contacts", contactService.getAll());
        model.addAttribute("orgaUnits", organisationUnitService.getAllExceptAdminUnit());
        model.addAttribute("position", LoginInfo.getPosition().toString());
        return "projectform";
    }

    @RequestMapping("/projects")
    public String showProjectList(Model model) {
        try {
            // List specific attributes
            model.addAttribute(LIST_FRAGMENT, "projectList");
            model.addAttribute(GRID_FRAGMENT, "projectGrid");
            model.addAttribute(DOMAIN, PROJECT);

            //Domain/fragment specific attributes
            List<Project> projects = projectService.getAll();
            projects.removeAll(projectService.getByRunning(Boolean.FALSE));
            projects.removeAll(projectService.getByEndDateIsPast());

            Map<Integer, Contact> contacts = new HashMap();
            Map<Integer, Client> clients = new HashMap<>();
            Map<Integer, Employee> managers = new HashMap<>();
            Map<Integer, OrganisationUnit> units = new HashMap<>();
            projects.forEach(project1 -> {
                Contact contact = contactService.findOne(project1.getContactId());
                contacts.put(project1.getId(), contact);
                clients.put(project1.getId(), clientService.findOne(contact.getClientId()));
                managers.put(project1.getId(), employeeService.findOne(project1.getProjectManagerId()));
                units.put(project1.getId(), organisationUnitService.findOne(project1.getOrgaUnitId()));
            });

            model.addAttribute(UNITS, units);
            model.addAttribute(MANAGER, managers);
            model.addAttribute(CLIENTS, clients);
            model.addAttribute(CONTACTS, contacts);
            model.addAttribute(PROJECTS, projects);
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing project list.", e);
            throw e;
        }
        return LIST;
    }


    @RequestMapping("/projectsHistory")
    public String showProjectHistoryList(Model model) {
        try {
            // List specific attributes
            model.addAttribute(LIST_FRAGMENT, "projectHistoryList");
            model.addAttribute(GRID_FRAGMENT, "projectHistoryGrid");
            model.addAttribute(DOMAIN, "projectHistory");

            //Domain/fragment specific attributes
            List<Project> projects = projectService.getByEndDateIsPast();


            Map<Integer, Contact> contacts = new HashMap();
            Map<Integer, Client> clients = new HashMap<>();
            Map<Integer, Employee> managers = new HashMap<>();
            Map<Integer, OrganisationUnit> units = new HashMap<>();
            projects.forEach(project1 -> {
                Contact contact = contactService.findOne(project1.getContactId());
                contacts.put(project1.getId(), contact);
                clients.put(project1.getId(), clientService.findOne(contact.getClientId()));
                managers.put(project1.getId(), employeeService.findOne(project1.getProjectManagerId()));
                units.put(project1.getId(), organisationUnitService.findOne(project1.getOrgaUnitId()));
            });

            model.addAttribute(UNITS, units);
            model.addAttribute(MANAGER, managers);
            model.addAttribute(CLIENTS, clients);
            model.addAttribute(CONTACTS, contacts);
            model.addAttribute(PROJECTS, projects);
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing project list.", e);
            throw e;
        }
        return LIST;
    }


    /**
     * Processing of a project form.
     *
     * @param newProjectCmd command
     * @param bindingResult binding result
     * @param model         model
     * @return projectform if invalid, projectlist if successive
     */
    @RequestMapping(value = "/projectform", method = RequestMethod.POST)
    public String saveProject(@Valid NewProjectCmd newProjectCmd,
                              BindingResult bindingResult,
                              Model model) throws PermissionDeniedException {
        try {
            if (bindingResult.hasErrors()) {
                log.debug("New project form input not valid.");
            } else {
                Project project = projectService.create(newProjectCmd);
                newProjectCmd.setId(project.getId().toString());
                needsSkillService.removeNeedsSkills(newProjectCmd);
                needsSkillService.createNeedsSkills(newProjectCmd);
                simpleLockService.releaseLock(new DomainIdentifier(project.getId(), Project.class));
                return "redirect:/singleproject?id=" + project.getId();
            }
        } catch (Exception e) {
            log.error("Error evaluating project form.", e);
            throw e;
        }

        Map<String, List<Employee>> organisationUnitManagerMap = new TreeMap<>();
        employeeService.getAllExceptAdmins().forEach(employee -> {
            String organisationUnit = organisationUnitService.findOne(employee.getOrganisationUnitId()).getName();
            if (!organisationUnitManagerMap.containsKey(organisationUnit)) {
                organisationUnitManagerMap.put(organisationUnit, new LinkedList<>());
            }
            organisationUnitManagerMap.get(organisationUnit).add(employee);
        });
        model.addAttribute("organisationUnitManager", organisationUnitManagerMap);
        model.addAttribute("skills", skillService.getAll());
        model.addAttribute("contacts", contactService.getAll());
        model.addAttribute("orgaUnits", organisationUnitService.getAllExceptAdminUnit());
        model.addAttribute("position", LoginInfo.getPosition().toString());
        model.addAttribute("newProjectCmd", newProjectCmd);
        return "projectform";
    }

    @RequestMapping(value = "/convert", method = RequestMethod.GET)
    public String convertProject(@RequestParam(value = "id", required = true) String id,
                                 Model model) throws PermissionDeniedException {
        Project project = projectService.findOne(Integer.valueOf(id));
        try {
            projectService.convert(project);
        } catch (Exception e) {
            log.error("Error converting project.", e);
            throw e;
        }
        return "redirect:/singleproject?id=" + project.getId();
    }

}
