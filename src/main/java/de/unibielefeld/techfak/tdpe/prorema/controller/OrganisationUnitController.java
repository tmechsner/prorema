package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewOrganisationUnitCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

import java.util.List;

import static de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool.skill;

/**
 * Created by Martin
 */
@Controller
@Log4j2
public class OrganisationUnitController extends ErrorHandler {

    private final OrganisationUnitService organisationUnitService;
    private final EmployeeService employeeService;
    private final ProjectService projectService;

    @Autowired
    public OrganisationUnitController(OrganisationUnitService organisationUnitService, EmployeeService employeeService, ProjectService projectService) {
        this.organisationUnitService = organisationUnitService;
        this.employeeService = employeeService;
        this.projectService = projectService;
    }

    @RequestMapping("/organistionUnitlist")
    public String showOrganisationUnitList(Model model) {
        try {
            model.addAttribute("organisationUnit", organisationUnitService.getAll());
        } catch (Exception e) {
            log.error("Error showing organisationUnitList.", e);
            throw e;
        }

        return "organisationUnitList";
    }

    @RequestMapping(value = "/organisationUnitform", method = RequestMethod.GET)
    public String showOrganisationUnitForm(NewOrganisationUnitCmd newOrganisationUnitCmd, Model model,
                                @RequestParam(value = "id", required = false) String id) {
        try {
            if (id != null) {
                OrganisationUnit organisationUnit = organisationUnitService.findOne(Integer.valueOf(id));
                if (organisationUnit != null) {
                    newOrganisationUnitCmd.setId(id);
                    newOrganisationUnitCmd.setName(organisationUnit.getName());
                    newOrganisationUnitCmd.setDescription(organisationUnit.getDescription());
                    model.addAttribute(newOrganisationUnitCmd);
                }
            }
        } catch (Exception e) {
            log.error("Error showing organisationUnitform.", e);
            throw e;
        }
        return "/organisationUnitform";
    }

    @RequestMapping(value = "/organisationUnitform", method = RequestMethod.POST)
    public String saveOrganisationUnitForm(@Valid NewOrganisationUnitCmd newOrganisationUnitCmd, BindingResult bindingResult, Model model) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("newOrganisationUnitCmd", newOrganisationUnitCmd);
                log.warn("OrganisationUni Bindingresult has errors!");
                return "organisationUnitform";
            } else {
                OrganisationUnit organisationUnit = organisationUnitService.createOrganisationUnit(newOrganisationUnitCmd);
                return "redirect:/organisationUnit/" + organisationUnit.getId();
            }
        } catch (Exception e) {
            log.error("Error evaluating organisationUnitform.");
            throw e;
        }
    }

    /**
    @RequestMapping(value = "/organisationUnit/{id}")
    public String showOrganisationUnitProfil(@PathVariable int id, Model model) {
        try {
            OrganisationUnit organisationUnit = organisationUnitService.findOne(id);
            model.addAttribute("organisationUnit", organisationUnit);
            List<Employee> employee = employeeService.findAll(organisationUnit.getEmployeeIds());
            model.addAttribute("employees", employee);
            List<Project> projects = projectService.findAll(organisationUnit.getProjectIds());
        } catch (Exception e) {
            log.error("Error showing contact profile.", e);
        }
        return "organisationUnitProfile";
    }
    */
}
