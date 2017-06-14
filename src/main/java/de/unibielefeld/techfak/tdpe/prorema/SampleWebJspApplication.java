package de.unibielefeld.techfak.tdpe.prorema;

import de.unibielefeld.techfak.tdpe.prorema.security.ReadSecurityConfig;
import lombok.extern.log4j.Log4j2;
import org.h2.server.web.WebServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

import java.util.Locale;

/**
 * Created by deisberg on 02/04/16.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableJpaRepositories
@EnableTransactionManagement
@EnableCaching
@EnableScheduling
@Log4j2
public class SampleWebJspApplication extends SpringBootServletInitializer {

    public SampleWebJspApplication() {

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SampleWebJspApplication.class);
    }

    /**
     * Bean adding error pages.
     *
     * @return bean.
     */
    @Bean
    public EmbeddedServletContainerCustomizer customizeErrorPage() {
        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer configurableEmbeddedServletContainer) {
                configurableEmbeddedServletContainer
                        .addErrorPages(new ErrorPage(HttpStatus.UNAUTHORIZED, "/error/unauthorized.html"));
                configurableEmbeddedServletContainer
                        .addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/error/unauthorized.html"));
                configurableEmbeddedServletContainer
                        .addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error/not_found.html"));
                configurableEmbeddedServletContainer
                        .addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error/error.html"));
            }
        };
    }

    /**
     * Bean loading l10n messages.
     *
     * @return bean
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource rrbms = new ReloadableResourceBundleMessageSource();
        rrbms.setBasenames("i18n/messages", "i18n/datatables");
        rrbms.setFallbackToSystemLocale(false);
        rrbms.getMessage("domain.contact", null, Locale.GERMAN);  // Without these the messages won't get loaded.
        rrbms.getMessage("domain.client", null, Locale.ENGLISH); //  One for every available language!
        return rrbms;
    }

    @Bean
    public Java8TimeDialect java8TimeDialect() {
        return new Java8TimeDialect();
    }

    public static void main(String[] args) throws Exception {
        ReadSecurityConfig.load();
        SpringApplication.run(SampleWebJspApplication.class, args);
    }

}