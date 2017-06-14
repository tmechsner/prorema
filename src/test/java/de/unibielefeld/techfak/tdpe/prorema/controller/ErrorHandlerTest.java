package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import org.hibernate.mapping.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ErrorHandlerTest {

    @InjectMocks
    ErrorHandler errorHandler;

    @Test
    public void errorHandler() throws Exception {
        Model model = mock(Model.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        Exception exception = mock(Exception.class);
        StringBuffer stringBuffer = new StringBuffer();
        given(req.getRequestURL()).willReturn(stringBuffer);

        String result = errorHandler.errorHandler(model, req, exception);

        assertThat(result).isEqualTo("error/error");
        verify(model).addAttribute("exception", exception);
        verify(model).addAttribute("url", stringBuffer);
        verify(model).addAllAttributes(anyMap());

    }

    @Test
    public void permissionDenied() throws Exception {
   /**     Model model = mock(Model.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        Exception exception = mock(Exception.class);
        StringBuffer stringBuffer = new StringBuffer();
        given(req.getRequestURL()).willReturn(stringBuffer);

        String result = errorHandler.errorHandler(model, req, exception);

        assertThat(result).isEqualTo("error/unauthorized");
        verify(model).addAttribute("exception", exception);
        verify(model).addAttribute("url", stringBuffer);
        verify(model).addAllAttributes(anyMap());
    **/}


}
