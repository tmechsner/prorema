package de.unibielefeld.techfak.tdpe.prorema.locking;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
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
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by Martin
 */
@Log4j2
public class WorksOnLockInterceptor extends HandlerInterceptorAdapter {

    /**
     * Name of the model attribute of a lock.
     */
    public static final String LOCK_ATTRIBUTE_NAME = "worksOnLock";

    /**
     * RequestParameter name containing the id of the domain.
     */
    private static final String ID_PARAMETER = "id";

    private final WorksOnLockService worksOnLockService;

    public WorksOnLockInterceptor (WorksOnLockService worksOnLockService) {

        this.worksOnLockService = worksOnLockService;

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        try {
            String uri = request.getRequestURI();
            if (uri.equalsIgnoreCase("/worksOnForm"))
            {
                Employee user = LoginInfo.getCurrentLogin();
                if(user != null)
                {
                    Integer OrgaId = user.getOrganisationUnitId();
                    WorksOnLock lock = worksOnLockService.getLock(OrgaId);

                //Lock action decision
                    if (lock != null) {
                        if (lock.getBeginDateTime().plus(lock.getDuration()).compareTo(LocalDateTime.now()) < 0) {
                            //If lock is expired, release it and create a new for the current user
                            worksOnLockService.releaseLock(OrgaId);
                            worksOnLockService.lockResource(user.getId(), OrgaId);
                            return true;
                        }
                        Integer holderId = lock.getHolderId();
                        if (!holderId.equals(user.getId())) {
                            //Send the lock through the redirect with a FlashMap
                            FlashMap flashMap = new FlashMap();
                            flashMap.put(LOCK_ATTRIBUTE_NAME, lock); // Prepare FlashMap
                            FlashMapManager flashMapManager = RequestContextUtils
                                    .getFlashMapManager(request); // Get manager of current request
                            if (flashMapManager != null) {
                                flashMapManager
                                        .saveOutputFlashMap(flashMap, request, response); // Add FlashMap to response
                            }

                            response.sendRedirect("/worksOnLocked");
                            return false;
                        }
                    } else {
                        worksOnLockService.lockResource(user.getId(), OrgaId);
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
            if (uri.equalsIgnoreCase("/worksOnForm")) {
                Employee user = LoginInfo.getCurrentLogin();
                if (user != null) {
                    Integer OrgaId = user.getOrganisationUnitId();
                    WorksOnLock lock = worksOnLockService.getLock(OrgaId);

                        //Lock action decision
                        if (lock != null) {
                            Integer holderId = lock.getHolderId();
                            if (holderId.equals(user.getId())) {
                                modelAndView.getModelMap().addAttribute(LOCK_ATTRIBUTE_NAME, lock);
                            }
                        }
                    }
                }
            }catch(Exception e){
                log.error("Error getting lock information.", e);
                throw e;
            }
        }
    }
