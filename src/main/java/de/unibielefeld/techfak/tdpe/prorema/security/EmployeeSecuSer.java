package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Matthias on 4/22/16.
 */

/**
 * Implements a UserDetailService for the Security Subsystem.
 * <p>
 * You can get the UserDetails-object by giving the user-name.
 * The data is retrieved from the EmployeeService
 * </p>
 */
@Service
@Log4j2
public class EmployeeSecuSer implements UserDetailsService {
    private EmployeeRepository rep;

    @Autowired
    public EmployeeSecuSer(EmployeeRepository rep) {
        this.rep = rep;
    }

    /**
     * This fetches the login-data from behind the security system,
     * as this will deny all accesses when no user is logged in.
     *
     * @return a list with all employees.
     */

    private List<Employee> getAllLogins() {
        List<Employee> result = new ArrayList<>();
        Iterable<EmployeeEntity> all = rep.findAll();
        for (EmployeeEntity entity : all) {
            result.add(new Employee(entity));
        }
        return result;
    }

    public List<Employee> getLogin( Predicate<Employee> filter) {
        return getAllLogins().parallelStream().filter(filter).collect(Collectors.toList());
    }


    /**
     * Searches for a user by its name.
     * <p>
     * The search is case-insensitive
     * </p>
     * @param s the username to search for
     * @return the object describing the user.
     * @throws UsernameNotFoundException thrown when no user was found.
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        List<Employee> users = getLogin(u -> u.getUsername().equalsIgnoreCase(s));
        if (users.size() != 1) {
            log.warn("User not found");
            throw new UsernameNotFoundException("User " + s + " was not found!");
        }
        Employee employee = users.get(0);
        log.info("New Login: User: " + employee.getUsername() + "Position: " + employee.getPosition().toString());
        return employee;
    }
}
