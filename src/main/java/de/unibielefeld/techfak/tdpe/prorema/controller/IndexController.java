package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Controller for the index page.
 */
@Controller
public class IndexController extends ErrorHandler {
    IndexController() {
    }

    @RequestMapping("/login")
    public String login(Map<String, Object> model) {
        return "login";
    }

}
