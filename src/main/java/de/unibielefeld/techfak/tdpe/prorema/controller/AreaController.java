package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewOrganisationUnitCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.locking.DomainIdentifier;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by jjakob on 06.06.16.
 */

@Controller
@Log4j2
public class AreaController {

    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final OrganisationUnitService organisationUnitService;
    private final SimpleLockService simpleLockService;

    @Autowired
    public AreaController(ProjectService projectService, EmployeeService employeeService,
                          OrganisationUnitService organisationUnitService, SimpleLockService simpleLockService) {
        this.projectService = projectService;
        this.employeeService = employeeService;
        this.organisationUnitService = organisationUnitService;
        this.simpleLockService = simpleLockService;
    }

    @RequestMapping(value = "/areaprofile", method = RequestMethod.GET)
    public String areaprofile(@RequestParam(name = "id") int id, Model model) {

        try {
            OrganisationUnit organisationUnit = organisationUnitService.findOne(id);
            if(organisationUnit != null) {
                model.addAttribute("orgaUnit", organisationUnit);

                Integer firstManagerId = organisationUnit.getFirstManagerId();
                if(!StringUtils.isEmpty(firstManagerId)) {
                    model.addAttribute("firstmanager", employeeService.findOne(firstManagerId));
                }

                Integer secondManagerId = organisationUnit.getSecondManagerId();
                if(!StringUtils.isEmpty(secondManagerId)) {
                    model.addAttribute("secondmanager", employeeService.findOne(secondManagerId));
                }

                List<Employee> employees = new ArrayList<>();
                employeeService.getByOrganisationUnit(organisationUnit.getId()).forEach(emp -> {
                    if ((emp.getId() != firstManagerId) &&
                            (emp.getId() != secondManagerId) &&
                            emp.getWorkExit().isAfter(LocalDate.now())) {
                        employees.add(emp);
                    }
                });
                model.addAttribute("employees", employees);
                List<Project> projects = new ArrayList<>();
                projectService.getAll().forEach(pro -> {
                    if (pro.getOrgaUnitId() == organisationUnit.getId()) {
                        projects.add(pro);
                    }
                });
                model.addAttribute("projects", projects);
            } else {
                log.error("OrganisationUnit with ID " + id + " not found!");
                model.addAttribute("error", "Der Bereich mit der ID " + id + " wurde nicht gefunden!");
            }

            List<ChangelogEntry> changelogs = organisationUnit.getHistory();
            Map<Integer, Employee> users = new HashMap<>();
            changelogs.forEach(changelogEntry -> {
                users.put(changelogEntry.getChangelogEntryId(), employeeService.findOne(changelogEntry.getUserId()));
            });
            model.addAttribute("changelogs", changelogs);
            model.addAttribute("users", users);

        } catch (Exception e) {
            log.error("Error showing profile.", e);
            throw e;
        }
        return "areaprofile";
    }


    @RequestMapping("/areas")
    public String showContactList(Model model) {
        try {
            model.addAttribute("listFragment", "areaList");
            model.addAttribute("gridFragment", "areaGrid");
            model.addAttribute("domain", "area");

            List<OrganisationUnit> organisationUnits = organisationUnitService.getAll();
            model.addAttribute("orgaUnits", organisationUnits);

            Map<Integer, Employee> firstmanager = new HashMap<>();
            Map<Integer, Employee> secondmanager = new HashMap<>();
            organisationUnits.forEach(orgU -> {
                firstmanager.put(orgU.getId(), employeeService.findOne(orgU.getFirstManagerId()));
                secondmanager.put(orgU.getId(), employeeService.findOne(orgU.getSecondManagerId()));
            });

            model.addAttribute("firstManMap", firstmanager);
            model.addAttribute("secondManMap", secondmanager);

            model.addAttribute("position", LoginInfo.getPosition().toString());

        } catch (Exception e) {
            log.error("Error showing project list.", e);
            throw e;
        }
        return "list";
    }


    @RequestMapping(value = "/areaform", method = RequestMethod.GET)
    public String showClientForm(NewOrganisationUnitCmd newOrganisationUnitCmd, Model model,
                                 @RequestParam(value = "id", required = false) String id,
                                 @RequestParam(value = "firstManagerId", required = false) String firstManagerId,
                                 @RequestParam(value = "secondMangerId", required = false) String secondManagerId) {

        try {
            if (id != null) {
                OrganisationUnit organisationUnit = organisationUnitService.findOne(Integer.valueOf(id));
                if (organisationUnit != null) {
                    newOrganisationUnitCmd.setId(id);
                    newOrganisationUnitCmd.setName(organisationUnit.getName());
                    newOrganisationUnitCmd.setDescription(organisationUnit.getDescription());
                    newOrganisationUnitCmd.setFirstManagerId(organisationUnit.getFirstManagerId().toString());
                    newOrganisationUnitCmd.setSecondManagerId(organisationUnit.getSecondManagerId().toString());
                    model.addAttribute(newOrganisationUnitCmd);
                } else {
                    log.error("OrganisationUnit with ID " + id + " not found!");
                    model.addAttribute("error", "Der Bereicht mit der ID " + id + " wurde nicht gefunden!");
                }
            }

            Map<String, List<Employee>> organisationUnitManagerMap = new TreeMap<>();
            makeOrgaUnitEmployeeMap(organisationUnitManagerMap);
            model.addAttribute("organisationUnitManager", organisationUnitManagerMap);
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing client form.", e);
            throw e;
        }
        return "areaform";
    }

    @RequestMapping(value = "/areaform", method = RequestMethod.POST)
    public String saveProject(@Valid NewOrganisationUnitCmd newOrganisationUnitCmd,
                              BindingResult bindingResult,
                              Model model) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("newOrganisationUnitCmd", newOrganisationUnitCmd);
                Map<String, List<Employee>> organisationUnitManagerMap = new TreeMap<>();
                makeOrgaUnitEmployeeMap(organisationUnitManagerMap);
                model.addAttribute("organisationUnitManager", organisationUnitManagerMap);
                model.addAttribute("position", LoginInfo.getPosition().toString());
                return "areaform";
            } else {
                OrganisationUnit organisationUnit = organisationUnitService
                        .createOrganisationUnit(newOrganisationUnitCmd);
                simpleLockService.releaseLock(new DomainIdentifier(organisationUnit.getId(), OrganisationUnit.class));
                return "redirect:/areaprofile?id=" + organisationUnit.getId();
            }
        } catch (Exception e) {
            log.error("Error evaluating project form.", e);
            throw e;
        }
    }

    private void makeOrgaUnitEmployeeMap(Map<String, List<Employee>> organisationUnitManagerMap) {
        employeeService.getAllNonExitedNonAdmin().forEach(employee -> {
            String organisationUnit = organisationUnitService.findOne(employee.getOrganisationUnitId()).getName();
            if (!organisationUnitManagerMap.containsKey(organisationUnit)) {
                organisationUnitManagerMap.put(organisationUnit, new LinkedList<>());
            }
            organisationUnitManagerMap.get(organisationUnit).add(employee);
        });
    }


}
