package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLock;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockInterceptor;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Benedikt Volkmer
 *         Created on 5/19/16.
 */
@Controller
@Log4j2
public class SimpleLockController extends ErrorHandler {

    private final SimpleLockService simpleLockService;
    private final EmployeeService employeeService;

    @Autowired SimpleLockController(SimpleLockService simpleLockService, EmployeeService employeeService) {
        this.simpleLockService = simpleLockService;
        this.employeeService = employeeService;
    }

    /**
     * Returns feedback about the lock.
     *
     * @param model autowired
     * @return lock feedback
     */
    @RequestMapping(value = "/locked")
    String resourceIsLocked(Model model) {
        try {
            SimpleLock lock = (SimpleLock) model.asMap().get(SimpleLockInterceptor.LOCK_ATTRIBUTE_NAME);
            model.addAttribute("endDate", lock.getBeginDateTime().plus(lock.getDuration()));
            Employee holder = employeeService.findOne(lock.getHolderId());
            model.addAttribute("holder",
                               String.join(" ", holder.getNameTitle(), holder.getFirstName(), holder.getLastName()));
            model.asMap().remove(SimpleLockInterceptor.LOCK_ATTRIBUTE_NAME);
        } catch (Exception e) {
            log.error("Error showing locking information.", e);
            throw e;
        }
        return "simpleLocked";
    }

}
