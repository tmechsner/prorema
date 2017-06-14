package de.unibielefeld.techfak.tdpe.prorema.locking;

import de.unibielefeld.techfak.tdpe.prorema.domain.Client;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Benedikt Volkmer
 *         Created on 5/22/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleLockInterceptorTest {

    private static final String LOCKED_URI = "/locked";

    //Dummy data
    private static final int CURRENT_USER_ID = 0;
    private static final String CURRENT_USER_NAME = "dummyname";

    //Mocks
    @Mock SimpleLockService simpleLockService;
    @Mock EmployeeService employeeService;
    @Mock List<Employee> employees;
    @Mock Employee employee;
    @Mock HttpServletRequest httpServletRequest;
    @Mock HttpServletResponse httpServletResponse;
    @Mock Object handler;
    @Mock SimpleLock lock;
    @Mock UserDetails userDetails;

    private SimpleLockInterceptor simpleLockInterceptor;

    @Before
    public void setUp() throws Exception {
        simpleLockInterceptor = new SimpleLockInterceptor(simpleLockService, employeeService);
        // Add valid current user to security context
        SecurityContextHolder.getContext()
                             .setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null));
        when(userDetails.getUsername()).thenReturn(CURRENT_USER_NAME);

        when(employeeService.getFiltered(any())).thenReturn(employees);
        when(employees.get(0)).thenReturn(employee);
        when(employee.getId()).thenReturn(CURRENT_USER_ID);
        when(employee.getUsername()).thenReturn(CURRENT_USER_NAME);
        when(httpServletRequest.getRequestURI()).thenReturn("/clientform");
        when(httpServletRequest.getParameter("id")).thenReturn("0");
    }

    @Test
    public void lockedByOtherUser() throws Exception {
        when(simpleLockService.getLock(any())).thenReturn(lock);
        when(lock.getHolderId()).thenReturn(CURRENT_USER_ID + 1); // aka. not the current user
        when(lock.getBeginDateTime()).thenReturn(LocalDateTime.now());
        when(lock.getDuration()).thenReturn(Duration.ofMinutes(30));

        assertFalse(simpleLockInterceptor.preHandle(httpServletRequest, httpServletResponse, handler));

        verify(httpServletResponse).sendRedirect(LOCKED_URI);
    }

    @Test
    public void lockedButExpired() throws Exception {
        when(simpleLockService.getLock(any())).thenReturn(lock);
        when(lock.getHolderId()).thenReturn(CURRENT_USER_ID + 1); // aka. not the current user
        when(lock.getBeginDateTime()).thenReturn(LocalDateTime.now().minusDays(1)); // aka. in the past
        when(lock.getDuration()).thenReturn(Duration.ofMinutes(30)); //aka. still in the past

        assertTrue(simpleLockInterceptor.preHandle(httpServletRequest, httpServletResponse, handler));

        verify(simpleLockService).releaseLock(eq(new DomainIdentifier(0, Client.class)));
        verify(simpleLockService).lockResource(eq(new DomainIdentifier(0, Client.class)), eq(CURRENT_USER_ID));
    }

    @Test
    public void lockedByCurrentUser() throws Exception {
        when(simpleLockService.getLock(any())).thenReturn(lock);
        when(lock.getHolderId()).thenReturn(CURRENT_USER_ID); // aka. the current user
        when(lock.getBeginDateTime()).thenReturn(LocalDateTime.now());
        when(lock.getDuration()).thenReturn(Duration.ofMinutes(30));

        assertTrue(simpleLockInterceptor.preHandle(httpServletRequest, httpServletResponse, handler));
    }

    @Test
    public void notLocked() throws Exception {
        when(simpleLockService.getLock(any())).thenReturn(null);

        assertTrue(simpleLockInterceptor.preHandle(httpServletRequest, httpServletResponse, handler));

        verify(simpleLockService).lockResource(eq(new DomainIdentifier(0, Client.class)), eq(CURRENT_USER_ID));
    }

}