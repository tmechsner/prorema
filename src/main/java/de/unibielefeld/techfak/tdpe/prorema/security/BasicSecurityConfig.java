package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider.SpringLoginProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Matthias on 4/20/16.
 * Handles the setup for Spring-Security-Framework
 * in a straight forward manner.
 */
@Configuration
@EnableWebSecurity
public class BasicSecurityConfig extends WebSecurityConfigurerAdapter {

    public BasicSecurityConfig() {

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                /* Allow access to static content for everyone*/
                .antMatchers("/css/**").permitAll()
                .antMatchers("/error/**").permitAll()
                .antMatchers("/fonts/**").permitAll()
                .antMatchers("/img/**").permitAll()
                .antMatchers("/js/**").permitAll()
		.antMatchers("/impressum").permitAll()

                /* For everything else you need to authorize yourself*/
                .anyRequest().hasAnyAuthority("USER", Employee.Position.PARTNER.toString(),
                Employee.Position.SENIOR_MANAGER.toString(),
                Employee.Position.MANAGER.toString(),
                Employee.Position.ADMINISTRATOR.toString())

                /*The login-form is allowed for everybody*/
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()

                .and()
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .permitAll();

    }

    /**
     * Global security configuration.
     *
     * @param emplServ    Service handling employees as users
     * @param passEncoder password encoder
     * @param auth        authentication manager builder
     * @throws Exception Some spring exceptions
     */
    @Autowired
    public void configureGlobal(EmployeeSecuSer emplServ,
                                PasswordEncoder passEncoder,
                                AuthenticationManagerBuilder auth) throws Exception {
        LoginInfo.setLoginProvider(new SpringLoginProvider());
        auth
                .userDetailsService(emplServ)
                .passwordEncoder(passEncoder);
    }


    @Bean PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public String encodePassword(String password) {
        return passwordEncoder().encode(password);
    }

    /**
     * Matcher for Configs that matches all Requests except for those starting with one of the given
     * allowedUrls-entries.
     */
    private static class AllExceptUrlsStartedWith implements RequestMatcher {

        private static final String[] ALLOWED_METHODS =
                new String[] { "GET", "HEAD", "TRACE", "OPTIONS" };

        private final String[] allowedUrls;

        AllExceptUrlsStartedWith(String... allowedUrls) {
            this.allowedUrls = allowedUrls;
        }

        @Override
        public boolean matches(HttpServletRequest request) {
            // replicate default behavior (see CsrfFilter.DefaultRequiresCsrfMatcher class)
            String method = request.getMethod();
            for (String allowedMethod : ALLOWED_METHODS) {
                if (allowedMethod.equals(method)) {
                    return false;
                }
            }

            // apply our own exceptions
            String uri = request.getRequestURI();
            for (String allowedUrl : allowedUrls) {
                if (uri.startsWith(allowedUrl)) {
                    return false;
                }
            }

            return true;
        }

    }
}
