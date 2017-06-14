package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.ConfigSyntaxError;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.function.Consumer;

/**
 * Created by Matthias on 6/18/16.
 * Reads the config file.
 */
@Log4j2
public class ReadSecurityConfig {

    public static String line;

    /**
     * Universal line reading funktion.
     *
     * @param reader   a reader which is read.
     * @param consumer process the line.
     * @throws IOException thrown by the reader, see its doc.
     */
    public static void readLine(Reader reader, Consumer<String> consumer) throws IOException {
        LineNumberReader r = new LineNumberReader(reader);
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            consumer.accept(line);
        }
    }

    /**
     * Main method for loading.
     */
    public static void load() {
        try {
            FileReader r = new FileReader("config.conf");
            readLine(r, s -> {
                try {
                    ParseSecurityConfig.load(s);
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    log.info("Syntax error");
                }
            });
        } catch (FileNotFoundException e) {
            log.info("No config file found");
        } catch (IOException e) {
            log.info("Error while reading file");
        }

    }


    /**
     * Find the next comma.
     *
     * @param args the string to parse.
     * @return the index of the next comma.
     */
    private static int findIndex(String args) {
        int index = args.indexOf(',');
        if (index != -1) {
            return index;
        }
        return args.length();
    }

    /**
     * Chops of the string at the index.
     *
     * @param str   the string.
     * @param index the index.
     * @return the suffix (index inclusive)
     */
    public static String choppOff(String str, int index) {
        if (index >= str.length())
            return "";
        return str.substring(index);
    }

    /**
     * Reads an actions parameter.
     *
     * @param args the string.
     * @return a tuple with the string (without the action) and the action
     * @throws ConfigSyntaxError
     */
    public static Tuple<String, Action> readAction(String args) throws ConfigSyntaxError {
        try {
            int index = findIndex(args);
            Action action = Action.fromString(args.substring(0, index));
            String str = choppOff(args, index + 1);
            return new Tuple<String, Action>(str, action);
        } catch (Exception ex) {
            throw new ConfigSyntaxError();
        }
    }

    /**
     * Reads a position parameter.
     *
     * @param args the string.
     * @return a tuple with the string (without the position) and the position
     * @throws ConfigSyntaxError
     */
    public static Tuple<String, Employee.Position> readPosition(String args) throws ConfigSyntaxError {
        try {
            int index = findIndex(args);
            Employee.Position pos = Employee.Position.fromString(args.substring(0, index));
            String str = choppOff(args, index + 1);
            return new Tuple<String, Employee.Position>(str, pos);
        } catch (Exception ex) {
            throw new ConfigSyntaxError();
        }
    }

    /**
     * Reads an integer parameter.
     *
     * @param args the string.
     * @return a tuple with the string (without the integer) and the interger
     * @throws ConfigSyntaxError
     */

    public static Tuple<String, Integer> readInteger(String args) throws ConfigSyntaxError {
        try {
            int index = findIndex(args);
            Integer i = Integer.parseInt(args.substring(0, index));
            String str = choppOff(args, index + 1);
            return new Tuple<String, Integer>(str, i);
        } catch (Exception ex) {
            throw new ConfigSyntaxError();
        }
    }
}
