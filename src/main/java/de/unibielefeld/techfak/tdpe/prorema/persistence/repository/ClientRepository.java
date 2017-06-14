package de.unibielefeld.techfak.tdpe.prorema.persistence.repository;

import de.unibielefeld.techfak.tdpe.prorema.persistence.ClientEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by JanLuca on 28.04.2016.
 */
public interface ClientRepository extends CrudRepository<ClientEntity, Integer> {
}
