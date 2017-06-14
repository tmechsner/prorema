package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewSkillCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.SkillService;
import de.unibielefeld.techfak.tdpe.prorema.locking.DomainIdentifier;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Brian Baumbach on 12.05.2016.
 */
@Controller
@Log4j2
public class SkillController {

    private final SkillService skillService;
    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final SimpleLockService simpleLockService;

    @Autowired
    public SkillController(SkillService skillService, EmployeeService employeeService, ProjectService projectService,
                           SimpleLockService simpleLockService) {
        this.skillService = skillService;
        this.employeeService = employeeService;
        this.projectService = projectService;
        this.simpleLockService = simpleLockService;
    }

    /**
     * Mapping for a skill list.
     *
     * @param model model
     * @return skilllist
     */
    @RequestMapping("/skillslist")
    public String showSkillsList(Model model) {
        try {
            model.addAttribute("listFragment", "skillList");
            model.addAttribute("gridFragment", "skillGrid");
            model.addAttribute("domain", "skill");

            model.addAttribute("skills", skillService.getAll());
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing skill list.", e);
            throw e;
        }

        return "list";
    }

    @RequestMapping(value = "/skillform", method = RequestMethod.GET)
    public String showSkillForm(NewSkillCmd newSkillCmd, Model model,
                                @RequestParam(value = "id", required = false) String id) {
        try {
            if (id != null) {
                Skill client = skillService.findOne(Integer.valueOf(id));
                if (client != null) {
                    newSkillCmd.setId(id);
                    newSkillCmd.setName(client.getName());
                    newSkillCmd.setDescription(client.getDescription());
                    model.addAttribute(newSkillCmd);
                }
            }
        } catch (Exception e) {
            log.error("Error showing client form.", e);
            throw e;
        }
        model.addAttribute("position", LoginInfo.getPosition().toString());
        return "/skillform";
    }

    /**
     * Processing of a skill form.
     *
     * @param newSkillCmd   command
     * @param bindingResult binding result
     * @param model         model
     * @return skillform if invalid, redirect to the skillprofile.
     */
    @RequestMapping(value = "/skillform", method = RequestMethod.POST)
    public String saveSkillForm(@Valid NewSkillCmd newSkillCmd, BindingResult bindingResult, Model model) throws
                                                                                                          PermissionDeniedException {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("newSkillCmd", newSkillCmd);
                model.addAttribute("position", LoginInfo.getPosition().toString());
                log.warn("Skill Bindingresult has errors!");
                return "skillform";
            } else {
                if (newSkillCmd.getDescription().equals("")) {
                    newSkillCmd.setDescription("Keine Beschreibung vorhanden");
                }
                Skill skill = skillService.createSkill(newSkillCmd);
                simpleLockService.releaseLock(new DomainIdentifier(skill.getId(), Skill.class));
                return "redirect:/skill?id=" + skill.getId();
            }
        } catch (Exception e) {
            log.error("Error evaluating skill form.");
            throw e;
        }
    }

    @RequestMapping(value = "/skill", method = RequestMethod.GET)
    public String showSkillProfil(@RequestParam(name = "id") int id, Model model) {
        try {
            Skill skill = skillService.findOne(id);
            model.addAttribute("skill", skill);
            try {
                List<Employee> employee = employeeService.findAll(skill.getEmployeeIds());
                model.addAttribute("employees", employee);
            } catch (PermissionDeniedException ex) { }
            try {
                List<Project> projects = projectService.findAll(skill.getProjectIds());
                model.addAttribute("projects", projects);
            } catch (PermissionDeniedException ex) {}
            model.addAttribute("position", LoginInfo.getPosition().toString());

            List<ChangelogEntry> changelogs = skill.getHistory();
            Map<Integer, Employee> users = new HashMap<>();
            changelogs.forEach(changelogEntry -> {
                users.put(changelogEntry.getChangelogEntryId(), employeeService.findOne(changelogEntry.getUserId()));
            });
            model.addAttribute("changelogs", changelogs);
            model.addAttribute("users", users);

        } catch (Exception e) {
            log.error("Error showing contact profile.", e);
            throw e;
        }
        return "skillProfile";
    }
}
