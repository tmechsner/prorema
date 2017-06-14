package de.unibielefeld.techfak.tdpe.prorema.persistence.audit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Creates changelogs for entities.
 * Listens on all persist, load, update and remove actions done by hibernate.
 * After an entity has been loaded, a map of property name -> object is created
 * with all loaded attribute values of the entity.
 * Another list will be created before update. Those lists will then be compared.
 * For each differing field that is not ignored
 * (on ignoredFields list or @Transient) a changelog entry will be created.
 * Persist and remove create simple CREATED or DELETED changelog entries.
 * Created by timo on 24.05.16.
 */
@Log4j2
@Service
public class AuditableEntityListener {

    /**
     * List of field names that will be ignored during the diff process.
     */
    private List<String> ignoredFields = Arrays
            .asList("id", "username", "password", "authorities", "informationpacker", "loadedvalues", "changelogentries");


    /**
     * Transform Entity to a Map of PropertyName => Object after every load action.
     * @param entity Entity that has just been loaded.
     */
    @PostLoad
    public void postLoad(AuditableEntity entity) {
        log.debug("PostLoad " + entity.getClass().getSimpleName());
        entity.setLoadedValues(getValues(entity));
    }

    /**
     * Log a deletion before each remove event.
     * @param entity Entity that is about to be removed.
     */
    @PreRemove
    public void preRemove(AuditableEntity entity) {
        log.debug("PreRemove " + entity.getClass().getSimpleName());
        EntityObserver.notifyDeletion(entity);
    }

    /**
     * Log a creation before each first persistence action.
     * @param entity Entity that ist about to be persisted for the first time.
     */
    @PrePersist
    public void prePersist(AuditableEntity entity) {
        log.debug("PrePersist " + entity.getClass().getSimpleName());
        EntityObserver.notifyCreation(entity);
    }

    /**
     * Compare loaded values with modified values and create changelog entries for differences.
     * @param entity Entity that is about to be updated.
     */
    @PreUpdate
    public void preUpdate(AuditableEntity entity) {
        log.debug("PreUpdate " + entity.getClass().getSimpleName());
        Map<String, Object> newValues = getValues(entity);
        Map<String, Object> oldValues = entity.getLoadedValues();
        if (oldValues == null) {
            log.warn("The old values for entity %s are null!", entity.toString());
            return;
        }
        for (Difference difference : getDifferences(oldValues, newValues)) {

            if (difference.getOldValue() != null) {
                EntityObserver
                        .notifyUpdate(entity, difference.getField(), difference.getOldValue(), difference.getNewValue());
            }
            else {
                EntityObserver
                        .notifyUpdateWithNull(entity, difference.getField(), difference.getNewValue());
            }
        }
    }

    /**
     * Checks differences in the values for each key. The Keyset should be identical.
     *
     * @param oldValues Field -> Value Map
     * @param newValues Field -> Value Map
     * @return A set of {@link Difference}s that contain the fieldname (key) and both values.
     */
    private Set<Difference> getDifferences(Map<String, Object> oldValues, Map<String, Object> newValues) {
        Set<Difference> differences = new HashSet<>();
        for (Entry<String, Object> entry : newValues.entrySet()) {
            Object oldValue = oldValues.get(entry.getKey());
            Object newValue = entry.getValue();
            String field = entry.getKey();
            if (oldValue == null) {
                if (newValue != null) {
                    differences.add(new Difference(field, null, newValue));
                }
            } else {
                if (!oldValue.equals(entry.getValue())) {
                    differences.add(new Difference(field, oldValue, newValue));
                }
            }
        }
        return differences;
    }

    /**
     * Get all non-ignored fields of the entity and put them in a map of PropertyName => Object.
     *
     * @param entity
     * @return
     */
    private Map<String, Object> getValues(AuditableEntity entity) {
        Class<?> clazz = entity.getClass();
        Map<String, Object> map = new HashMap<>();
        for (Field field : getAllFields(clazz)) {

            // Skip fields on blacklist and that are transient
            if (field.getAnnotation(Transient.class) != null ||
                ignoredFields.contains(field.getName().toLowerCase())) {
                continue;
            }

            // Construct getter method name and run the method
            String capitalizedFieldName = field.getName().substring(0, 1).toUpperCase()
                                               .concat(field.getName().substring(1));
            String getterName = "get" + capitalizedFieldName;
            String isName = "is" + capitalizedFieldName;
            MethodResult methodResult = runMethod(getterName, entity);

            // Success? Save result
            if (methodResult.isRun()) {
                map.put(field.getName(), methodResult.value);
                continue;

                // No Success? Try "is" instead of "get"
            } else {

                methodResult = runMethod(isName, entity);
                if (methodResult.isRun()) {
                    map.put(field.getName(), methodResult.value);
                    continue;

                    // Still no Success? Get the field directly
                } else {

                    field.setAccessible(true);
                    try {
                        map.put(field.getName(), field.get(entity));
                    } catch (IllegalArgumentException e) {
                        // Does nothing in this case
                    } catch (IllegalAccessException e) {
                        // Does nothing in this case
                    }
                }
            }
        }

        return map;
    }

    /**
     * Invoke method with name "name" on the entity.
     *
     * @param name
     * @param entity
     * @return
     */
    private MethodResult runMethod(String name, Object entity) {
        Method method = getMethod(entity.getClass(), name);
        if (method == null) {
            return MethodResult.didntRun();
        }

        try {
            return new MethodResult(true, method.invoke(entity));
        } catch (IllegalArgumentException e) {
            return MethodResult.didntRun();
        } catch (IllegalAccessException e) {
            return MethodResult.didntRun();
        } catch (InvocationTargetException e) {
            return MethodResult.didntRun();
        }

    }

    /**
     * Get all fields of a class: public, private and inherited.
     *
     * @param type Class to get fields of.
     * @return A list of all fields of the given class.
     */
    private List<Field> getAllFields(Class<?> type) {
        return getAllFields(new ArrayList<>(), type);
    }

    /**
     * Recursive part of the getAllFields method.
     *
     * @param fields
     * @param type
     * @return
     */
    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    /**
     * Get method with given name.
     *
     * @param clazz
     * @param getterName
     * @return
     */
    private Method getMethod(Class<?> clazz, String getterName) {
        Method method = null;
        try {
            method = clazz.getMethod(getterName);
        } catch (SecurityException e) {
            // Does nothing will try other thing
            method = null;
        } catch (NoSuchMethodException e) {
            // Does nothing will try other thing
            method = null;
        }
        return method;
    }

    /**
     * Container for result of method-run.
     */
    @Getter
    @Setter
    public static class MethodResult {

        /**
         * Did the method run successfully?
         */
        private boolean run;

        /**
         * Return value of the method.
         */
        private Object value = null;

        public MethodResult(boolean run, Object value) {
            this.run = run;
            this.value = value;
        }

        /**
         * Creates a MethodResult for a failed method run.
         * @return new MethodResult object
         */
        public static MethodResult didntRun() {
            return new MethodResult(false, null);
        }

    }

    /**
     * Stands for a difference in one field.
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Difference {
        private Object oldValue;
        private Object newValue;
        private String field;

        /**
         * Create a new Difference on the given field with old and new value.
         * @param field Field with a diff
         * @param oldValue old value
         * @param newValue new value
         */
        public Difference(String field, Object oldValue, Object newValue) {
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.field = field;
        }
    }
}
