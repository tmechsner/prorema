package de.unibielefeld.techfak.tdpe.prorema.security;


import de.unibielefeld.techfak.tdpe.prorema.security.deciders.Decider;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;

/**
 * Created by Matthias on 5/28/16.
 * This parses the security config file.
 */
@Log4j2
public class ParseSecurityConfig {

    /**
     * Loads the decider class
     *
     * @param name the decider string.
     * @param para the parameters for the decider
     * @return the initilized decider.
     */
    static Decider loadDecider(String name, String para) {
        Decider d;
        try {
            d = (Decider) Class.
                    forName("de.unibielefeld.techfak.tdpe.prorema.security.deciders." + name).newInstance();
        } catch (ClassCastException | IllegalAccessException | InstantiationException | ClassNotFoundException exp) {
            die(exp);
            return null;
        }
        try {
            d.init(para);
        } catch (Throwable error) {
            die(error);
            return null;
        }
        return d;

    }

    /**
     * Terminates the application.
     */
    private static void die(Throwable error) {
        log.error("Error while parsing the security config file");
        log.error(error.getMessage());
        log.error("To prevent unintended access to your data the server is shutting down!");
        error.printStackTrace();
        log.error(ReadSecurityConfig.line);
        System.exit(-1);
    }

    /**
     * Extracts the class string.
     * @param s the whole string.
     * @return the class string.
     */
    static String getClassString(String s) {
        String classString;
        int sI = s.indexOf('(');
        if (sI == -1) {
            classString = s;
        } else {
            classString = s.substring(0, sI);
        }
        return classString;
    }

    /**
     * Removes the first decider with parameters from the given string.
     * @param s the string.
     * @return the string without the decider.
     */
    private static String removeFirstDecider(String s) {
        int i = s.indexOf(')');
        if (i == -1) {
            return "";
        }
        return s.substring(i + 1);
    }

    /**
     * Creates an AccessDecider from the string.
     * @param s the string.
     * @return the AccessDecider represented by this string.
     */
    static public AccessDecider parseString(String s) {
        AccessDecider ad = new AccessDecider();
        Decider d = null;
        while (s.length() != 0) {
            d = loadDecider(getClassString(s), getParameterString(s));
            s = removeFirstDecider(s);
            ad.add(d);
        }
        return ad;
    }


    /**
     * Extracts the next parameter string.
     * @param s the string.
     * @return the parameter string.
     */
    public static String getParameterString(String s) {
        int start = s.indexOf('(');
        int end = s.indexOf(')');
        String paraString = "";
        if (start != -1) {
            paraString = s.substring(start + 1, end);
        }
        return paraString;
    }

    /**
     * Loads an AccessDecider into the AccessDeciderPool
     * @param s a line as in the configuration given.
     * @return the Decider loaded.
     * @throws NoSuchFieldException the AccessDecider you wanted to install is no in the pool.
     * @throws IllegalAccessException
     */
    public static AccessDecider load(String s) throws NoSuchFieldException, IllegalAccessException {
        int i = s.indexOf(':');
        String field = s.substring(0, i);
        s = s.substring(i + 1);
        AccessDecider ad = parseString(s);
        Field f = AccessDeciderPool.class.getField(field);
        f.set(null, ad);
        return ad;
    }
}
