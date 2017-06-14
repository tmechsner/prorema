package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Matthias on 5/27/16.
 */
public interface AbstactService<D , E> {
    /**
     * @return All Domains.
     */
    List<D> getAll();

    /**
     * @return All Entities.
     */
    List<E> getAllEntities();

    /**
     * @param ids Ids of projects to return.
     * @return List of projects to display.
     */
    List<D> findAll(Iterable<Integer> ids);

    /**
     * Finds exactly one entity by id.
     *
     * @param id the id to find
     * @return object with the id.
     */
    D findOne(Integer id);

    /**
     * Gets a filtered list of domains.
     *
     * @param filter filter to apply
     * @return filtered List
     */
    List<D> getFiltered(Predicate<D> filter);

    /**
     * Deletes entry from db.
     *
     * @param id id to delete
     * @return Success state. Also fails, if entry had not existed.
     */
    boolean delete(Integer id);
}
