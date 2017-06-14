package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewEmployeeCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.EmployeeProfile;
import de.unibielefeld.techfak.tdpe.prorema.domain.HasSkill;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeProfileService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.HasSkillService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.SkillService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.WorksOnService;
import de.unibielefeld.techfak.tdpe.prorema.locking.DomainIdentifier;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex Schneider.
 */
@Controller
@Log4j2
public class EmployeeController extends ErrorHandler {

    private EmployeeService employeeService;
    private EmployeeProfileService employeeProfileService;
    private OrganisationUnitService organisationUnitService;
    private SkillService skillService;
    private WorksOnService worksOnService;
    private ProjectService projectService;
    private HasSkillService hasSkillService;
    private final SimpleLockService simpleLockService;

    /**
     * Constructor.
     *
     * @param employeeService         autowired
     * @param employeeProfileService  autowired
     * @param worksOnService          autowired
     * @param projectService          autowired
     * @param organisationUnitService autowired
     * @param skillService            autowired
     * @param hasSkillService         autowired
     * @param simpleLockService       autowired
     */
    @Autowired
    public EmployeeController(EmployeeService employeeService, EmployeeProfileService employeeProfileService,
                              WorksOnService worksOnService, ProjectService projectService,
                              OrganisationUnitService organisationUnitService, SkillService skillService,
                              HasSkillService hasSkillService, SimpleLockService simpleLockService) {
        this.employeeService = employeeService;
        this.employeeProfileService = employeeProfileService;
        this.organisationUnitService = organisationUnitService;
        this.projectService = projectService;
        this.worksOnService = worksOnService;
        this.skillService = skillService;
        this.hasSkillService = hasSkillService;
        this.simpleLockService = simpleLockService;
    }

    /**
     * Mapping for user list.
     *
     * @param model autowired
     * @return userlist template
     */
    @RequestMapping("/userslist")
    public String userlist(Model model) {
        try {
            // List specific attributes
            model.addAttribute("listFragment", "userList");
            model.addAttribute("gridFragment", "userGrid");
            model.addAttribute("domain", "user");

            //Domain/fragment specific attributes
            List<Employee> employees = employeeService.getAllNonExited();
            employees
                    .removeAll(employeeService.getByPosition(Employee.Position.ADMINISTRATOR.toString().toLowerCase()));

            Map<Integer, OrganisationUnit> units = new HashMap<>();
            employees.forEach(employee -> {
                units.put(employee.getId(), organisationUnitService.findOne(employee.getOrganisationUnitId()));
            });

            model.addAttribute("unitMap", units);
            model.addAttribute("employees", employees);
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing project list.", e);
            throw e;
        }
        return "list";
    }


    /**
     * Mapping for user list.
     *
     * @param model autowired
     * @return userlist template
     */
    @RequestMapping("/userlistHistory")
    public String userlisthistory(Model model) {
        try {
            // List specific attributes
            model.addAttribute("listFragment", "userListHistory");
            model.addAttribute("gridFragment", "userGridHistory");
            model.addAttribute("domain", "userHistory");

            //Domain/fragment specific attributes
            List<Employee> employees = employeeService.getAllFormerEmployees();

            Map<Integer, OrganisationUnit> units = new HashMap<>();
            employees.forEach(employee -> {
                units.put(employee.getId(), organisationUnitService.findOne(employee.getOrganisationUnitId()));
            });

            model.addAttribute("unitMap", units);
            model.addAttribute("employees", employees);
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing project list.", e);
            throw e;
        }
        return "list";
    }


    /**
     * User profile template.
     *
     * @param id    optional id of the employee to edit
     * @param model autowired
     * @return user profile form template
     */
    @RequestMapping(value = "/userprofile", method = RequestMethod.GET)
    public String userprofile(@RequestParam(name = "id") int id, Model model) {
        try {
            Employee employee = employeeService.findOne(id);
            if (employee != null) {
                employee.setPassword("");
                model.addAttribute("employee", employee);

                OrganisationUnit orgaUnitOfEmpl = organisationUnitService.findOne(employee.getOrganisationUnitId());
                if (orgaUnitOfEmpl != null) {
                    model.addAttribute("orgaUnit", orgaUnitOfEmpl);
                }

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                List<EmployeeProfile> empProfiles = employeeProfileService
                        .findAll(employee.getEmployeeProfiles());
                model.addAttribute("profiles", empProfiles);
                List<Project> projects = new ArrayList<>();
                worksOnService.getWorksOnByEmployee(id).forEach(worksOn -> {
                    Project p = projectService.findOne(worksOn.getProjectId());
                    if (!projects.contains(p) && p.getId() != 7000) {
                        projects.add(p);
                    }
                });
                model.addAttribute("worksOnPro", projects);
                model.addAttribute("position", LoginInfo.getPosition().toString());
                LocalDate startDate = employee.getWorkEntry();
                if (startDate != null) {
                    model.addAttribute("workEntry", startDate.format(dateTimeFormatter));
                }
                LocalDate endDate = employee.getWorkExit();
                if (endDate != null) {
                    model.addAttribute("workExit", endDate.format(dateTimeFormatter));
                }

                List<ChangelogEntry> changelogs = employee.getHistory();
                Map<Integer, Employee> users = new HashMap<>();
                changelogs.forEach(changelogEntry -> {
                    users.put(changelogEntry.getChangelogEntryId(),
                              employeeService.findOne(changelogEntry.getUserId()));
                });
                model.addAttribute("changelogs", changelogs);
                model.addAttribute("users", users);
            } else {
                log.error("Employee with ID " + id + " not found!");
                model.addAttribute("error", "Der Mitarbeiter mit der ID " + id + " wurde nicht gefunden!");
            }
        } catch (Exception e) {
            log.error("Error showing user profile.", e);
            throw e;
        }
        return "userprofile";
    }

    /**
     * User profile processing mapping.
     *
     * @param newEmployeeCmd autowired
     * @param bindingResult  autowired
     * @param model          autowired
     * @return created profiles page
     * @throws PermissionDeniedException if permission to create/edit this employee is denied
     */
    @RequestMapping(value = "/userprofile", method = RequestMethod.POST)
    public String saveSkill(@Valid NewEmployeeCmd newEmployeeCmd, BindingResult bindingResult, Model model)
            throws PermissionDeniedException {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("newEmployeeCmd", newEmployeeCmd);
                return "userprofile";
            } else {
                employeeService.create(newEmployeeCmd);
                return "redirect:/userprofile";
            }
        } catch (Exception e) {
            log.error("Error evaluation employee form.", e);
            throw e;
        }
    }

    /**
     * Mapping to delete users.
     *
     * @param id id of user to delete
     * @return redirect to userlist
     */
    @RequestMapping("userprofile/delete")
    public String deleteEmployee(@RequestParam(name = "id") int id) {
        //skillService.deleteSkill(id);
        employeeService.delete(id);

        return "redirect:/userslist";
    }


    /**
     * Mapping for userform.
     *
     * @param newEmployeeCmd command for new employees
     * @param model          model
     * @param id             id of user to modify
     * @return userform
     */
    @RequestMapping(value = "/userform", method = RequestMethod.GET)
    public String showEmployeeform(NewEmployeeCmd newEmployeeCmd, Model model,
                                   @RequestParam(value = "id", required = false) String id) {


        try {
            if (id != null) {
                Employee employee = employeeService.findOne(Integer.valueOf(id));
                if (employee != null) {
                    newEmployeeCmd.setId(id);
                    newEmployeeCmd.setNameTitle(employee.getNameTitle());
                    newEmployeeCmd.setFirstName(employee.getFirstName());
                    newEmployeeCmd.setLastName(employee.getLastName());
                    newEmployeeCmd.setPosition(employee.getPosition().toString());
                    newEmployeeCmd.setTel(employee.getTel());
                    newEmployeeCmd.setEMail(employee.getMail());
                    newEmployeeCmd.setCity(employee.getCity());
                    newEmployeeCmd.setStreet(employee.getStreet());
                    newEmployeeCmd.setCountry(employee.getCountry());
                    newEmployeeCmd.setZip(employee.getZip());
                    newEmployeeCmd.setWorkSchedule(employee.getWorkSchedule());
                    newEmployeeCmd.setWorkEntry(employee.getWorkEntry().toString());
                    newEmployeeCmd.setWorkExit(employee.getWorkExit().toString());
                    newEmployeeCmd.setUsername(employee.getUsername());
                    Integer organisationUnit = employee.getOrganisationUnitId();
                    if (organisationUnit != null) {
                        newEmployeeCmd.setOrgaUnitId(organisationUnit.toString());
                    }

                    model.addAttribute(newEmployeeCmd);

                    List<HasSkill> hasSkillList = hasSkillService.findByEmployeeId(Integer.parseInt(id));
                    model.addAttribute("hasSkillList", hasSkillList);
                    List<EmployeeProfile> employeeProfiles = employeeProfileService
                            .findAll(employee.getEmployeeProfiles());
                    model.addAttribute("profilesList", employeeProfiles);

                }
            }

            model.addAttribute("orgaUnits", organisationUnitService.getAll());
            model.addAttribute("skills", skillService.getAll());
            model.addAttribute("profiles", employeeProfileService.getAll());
            model.addAttribute("position", LoginInfo.getPosition().toString());

        } catch (Exception e) {
            log.error("Error showing employee form.", e);
            throw e;
        }

        return "userform";
    }


    /**
     * Mapping to process userform input.
     *
     * @param newEmployeeCmd command
     * @param bindingResult  binding result
     * @param model          model
     * @return userform if invalid, userlist if successive
     * @throws PermissionDeniedException if permission to create/edit this employee is denied
     */
    @RequestMapping(value = "/userform", method = RequestMethod.POST)
    public String saveEmployee(@Valid NewEmployeeCmd newEmployeeCmd, BindingResult bindingResult, Model model)
            throws PermissionDeniedException {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("orgaUnits", organisationUnitService.getAll());
                model.addAttribute("skills", skillService.getAll());
                // model.addAttribute("profiles", employeeProfileService.getAll());
                model.addAttribute("newEmployeeCmd", newEmployeeCmd);
                model.addAttribute("position", LoginInfo.getPosition().toString());
                return "userform";
            } else {
                Employee employee = employeeService.create(newEmployeeCmd);
                newEmployeeCmd.setId(employee.getId().toString());
                hasSkillService.removeHasSkills(newEmployeeCmd);
                hasSkillService.createHasSkills(newEmployeeCmd);
                simpleLockService.releaseLock(new DomainIdentifier(employee.getId(), Employee.class));
                return "redirect:/userprofile?id=" + employee.getId();
            }
        } catch (Exception e) {
            log.error("Error evaluation employee form.", e);
            throw e;
        }
    }
}
