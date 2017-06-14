package de.unibielefeld.techfak.tdpe.prorema.locking;

import de.unibielefeld.techfak.tdpe.prorema.domain.Client;
import de.unibielefeld.techfak.tdpe.prorema.domain.Contact;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.Skill;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Intercepting on requests targeting domains protected by simple locking.
 * <p>
 * It is intercepting on forms defined in SIMPLY_LOCKED_URIS. It creates a lock if none is existing and redirects
 * to
 * "\locked", if there is a lock the current user is not holder of.
 * </p>
 *
 * @return bean
 */
@Log4j2
public class SimpleLockInterceptor extends HandlerInterceptorAdapter {


    /**
     * Name of the model attribute of a lock.
     */
    public static final String LOCK_ATTRIBUTE_NAME = "simpleLock";
    private static final Map<String, Class> SIMPLY_LOCKED_URIS;
    /**
     * RequestParameter name containing the id of the domain.
     */
    private static final String ID_PARAMETER = "id";

    private final SimpleLockService simpleLockService;
    private final EmployeeService employeeService;

    static {
        Map<String, Class> map = new LinkedHashMap<>();
        map.put("/clientform", Client.class);
        map.put("/contactform", Contact.class);
        map.put("/areaform", OrganisationUnit.class);
        map.put("/userform", Employee.class);
        map.put("/projectform", Project.class);
        map.put("/skillform", Skill.class);
        SIMPLY_LOCKED_URIS = map;
    }

    public SimpleLockInterceptor(SimpleLockService simpleLockService, EmployeeService employeeService) {
        this.simpleLockService = simpleLockService;
        this.employeeService = employeeService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            String uri = request.getRequestURI();
            String idParameter = request.getParameter(ID_PARAMETER);
            if (SIMPLY_LOCKED_URIS.containsKey(uri)
                && idParameter != null && !idParameter.isEmpty()) {
                SecurityContext ctx = SecurityContextHolder.getContext();
                Authentication auth = ctx.getAuthentication();
                Object currentUserObject = auth.getPrincipal();
                if (currentUserObject instanceof UserDetails) {
                    Predicate<Employee> compareUsernames = employee -> employee.getUsername().equalsIgnoreCase(
                            ((UserDetails) currentUserObject).getUsername());
                    Integer currentUserId = employeeService.getFiltered(compareUsernames).get(0).getId();
                    DomainIdentifier domainIdentifier = new DomainIdentifier(
                            Integer.valueOf(idParameter),
                            SIMPLY_LOCKED_URIS.get(uri));
                    SimpleLock lock = simpleLockService.getLock(domainIdentifier);

                    //Lock action decision
                    if (lock != null) {
                        if (lock.getBeginDateTime().plus(lock.getDuration()).compareTo(LocalDateTime.now()) < 0) {
                            //If lock is expired, release it and create a new for the current user
                            simpleLockService.releaseLock(domainIdentifier);
                            simpleLockService.lockResource(domainIdentifier, currentUserId);
                            return true;
                        }
                        Integer holderId = lock.getHolderId();
                        if (!holderId.equals(currentUserId)) {
                            //Send the lock through the redirect with a FlashMap
                            FlashMap flashMap = new FlashMap();
                            flashMap.put(LOCK_ATTRIBUTE_NAME, lock); // Prepare FlashMap
                            FlashMapManager flashMapManager = RequestContextUtils
                                    .getFlashMapManager(request); // Get manager of current request
                            if (flashMapManager != null) {
                                flashMapManager
                                        .saveOutputFlashMap(flashMap, request, response); // Add FlashMap to response
                            }

                            response.sendRedirect("/locked");
                            return false;
                        }
                    } else {
                        simpleLockService.lockResource(domainIdentifier, currentUserId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting lock information.", e);
            throw e;
        }

        return true;
    }

    @Override public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                                     ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);

        try {
            String uri = request.getRequestURI();
            String idParameter = request.getParameter(ID_PARAMETER);
            if (SIMPLY_LOCKED_URIS.containsKey(uri)
                && idParameter != null && !idParameter.isEmpty()) {
                SecurityContext ctx = SecurityContextHolder.getContext();
                Authentication auth = ctx.getAuthentication();
                Object currentUserObject = auth.getPrincipal();
                if (currentUserObject instanceof UserDetails) {
                    Predicate<Employee> compareUsernames = employee -> employee.getUsername().equalsIgnoreCase(
                            ((UserDetails) currentUserObject).getUsername());
                    Integer currentUserId = employeeService.getFiltered(compareUsernames).get(0).getId();
                    DomainIdentifier domainIdentifier = new DomainIdentifier(
                            Integer.valueOf(idParameter),
                            SIMPLY_LOCKED_URIS.get(uri));
                    SimpleLock lock = simpleLockService.getLock(domainIdentifier);

                    //Lock action decision
                    if (lock != null) {
                        Integer holderId = lock.getHolderId();
                        if (holderId.equals(currentUserId)) {
                            modelAndView.getModelMap().addAttribute(LOCK_ATTRIBUTE_NAME, lock);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting lock information.", e);
            throw e;
        }
    }
}

