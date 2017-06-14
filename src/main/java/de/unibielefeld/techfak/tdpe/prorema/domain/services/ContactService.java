package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewContactCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Contact;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ContactEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;

/**
 * ContactService.
 */
public interface ContactService extends AbstactService<Contact , ContactEntity> {
    /**
     * create a contact from user input.
     * creates a database entry.
     *
     * @param command Command with user input
     * @return The created contact.
     */
    Contact createContact(NewContactCmd command) throws PermissionDeniedException;
}
