package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by Matthias on 5/25/16.
 * We want to have some general information to the frontend
 * so that some buttons can be hidden.
 */
public class ModelEnhancer extends HandlerInterceptorAdapter {
    /**
     * Adds general security info for Controllers.
     * @param model the model to enhance.
     */
    public static Map<String, Object> getGeneralInfo(Map<String,Object> model) {
        model.put("allowedCreateProject",
                AccessDeciderPool.project.isAllowed(Action.CREATE, null) );

        model.put("allowedCreateEmployee",
                AccessDeciderPool.employee.isAllowed(Action.CREATE, null) );

        model.put("allowedCreateSkill",
                AccessDeciderPool.skill.isAllowed(Action.CREATE, null) );

        model.put("allowedCreateContact",
                AccessDeciderPool.contact.isAllowed(Action.CREATE, null) );

        model.put("allowedCreateClient",
                AccessDeciderPool.client.isAllowed(Action.CREATE, null) );

        model.put("allowedCreateOrgaUnit",
                AccessDeciderPool.organisationUnit.isAllowed(Action.CREATE, null) );

        return model;
    }


    /**
     * Called by spring inceptor defined in config.SecurityConfig.
     * @param request see spring doc...
     * @param response see spring doc...
     * @param handler see spring doc...
     * @param modelAndView see spring doc...
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView.getViewName().startsWith("redirect:") ||
            LoginInfo.getCurrentLogin() == null) {
            return;
        }

        modelAndView.getModelMap().addAllAttributes(getGeneralInfo(modelAndView.getModel()));

        try {
            modelAndView.getModelMap().addAttribute("loggedInUserId", LoginInfo.getCurrentLogin().getId());
        } catch (NullPointerException e) {
            modelAndView.getModelMap().addAttribute("loggenInUserId", 0);
        }
    }
}
