package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewClientCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Client;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ClientEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;

/**
 * ClientService.
 */
public interface ClientService extends AbstactService<Client, ClientEntity> {

    /**
     * create a client from user input.
     * creates a database entry.
     *
     * @param command Command with user input
     * @return The created client.
     */
    Client createClient(NewClientCmd command) throws PermissionDeniedException;
}
