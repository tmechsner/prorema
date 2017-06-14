package de.unibielefeld.techfak.tdpe.prorema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Currently only add support for i18n and l10n
 * Created by Benedikt Volkmer on 5/12/16.
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    WebMvcConfig() {
    }

    /**
     * Bean adding locale resolving for l10n.
     *
     * @return bean
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.GERMAN);
        slr.setDefaultTimeZone(TimeZone.getDefault());
        return slr;
    }

    /**
     * Bean adding the ability to change the local for l10n.
     *
     * @return bean
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }


    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        super.addInterceptors(registry);
    }
}
