package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.ModelEnhancer;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@Controller
@Log4j2
@NoArgsConstructor
public class ErrorHandler {

    @ExceptionHandler(Exception.class)
    public String errorHandler(Model model, HttpServletRequest req, Exception exception) {
        log.error("Request: " + req.getRequestURL() + " raised " + exception);
        model.addAttribute("exception", exception);
        model.addAttribute("url", req.getRequestURL());
        model.addAllAttributes(ModelEnhancer.getGeneralInfo(model.asMap()));
        return "error/error";
    }


    @ExceptionHandler(PermissionDeniedException.class)
    public String permissionDenied(Model model, HttpServletRequest req, Exception exception) {
        log.error("Request: " + req.getRequestURL() + " raised " + exception);
        model.addAttribute("exception", exception);
        model.addAttribute("url", req.getRequestURL());
        model.addAllAttributes(ModelEnhancer.getGeneralInfo(model.asMap()));
        return "error/unauthorized";
    }
}
