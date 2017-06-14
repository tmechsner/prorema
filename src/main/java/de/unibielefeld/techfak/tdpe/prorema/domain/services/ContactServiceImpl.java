package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewContactCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Contact;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ClientEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ContactEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ClientRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ContactRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * ContactServiceImpl.
 */
@Service
@Primary
public class ContactServiceImpl
        extends AbstactServiceImpl<Contact, ContactRepository, ContactEntity>
        implements ContactService {

    private final ClientRepository clientRepository;

    @Autowired
    public ContactServiceImpl(ContactRepository repository, ClientRepository clientRepository) {
        super(repository);
        this.clientRepository = clientRepository;
        accessDecider = AccessDeciderPool.contact;
    }

    @Override
    protected Contact init(ContactEntity entity) {
        return new Contact(entity);
    }

    @Override
    public Contact createContact(NewContactCmd command) throws PermissionDeniedException {
        if (command != null) {
            ContactEntity contactEntity;
            if (command.getId() != null && !command.getId().isEmpty()
                && repository.exists(Integer.valueOf(command.getId()))) {
                accessDecider.isAllowedThrow(Action.MODIFY, null);
                contactEntity = repository.findOne(Integer.valueOf(command.getId()));
                contactEntity.setNameTitle(command.getNameTitle());
                contactEntity.setFirstName(command.getFirstName());
                contactEntity.setLastName(command.getLastName());
                contactEntity.setEmail(command.getEmail());
                contactEntity.setTel(command.getTel());
                contactEntity.setStreet(command.getStreet());
                contactEntity.setZip(command.getZip());
                contactEntity.setCity(command.getCity());
                contactEntity.setCountry(command.getCountry());
            } else {
                accessDecider.isAllowedThrow(Action.CREATE, null);
                contactEntity = new ContactEntity(command.getNameTitle(), command.getFirstName(),
                                                  command.getLastName(), command.getTel(), command.getEmail(),
                                                  command.getStreet(), command.getZip(), command.getCity(),
                                                  command.getCountry());
            }
            ClientEntity clientEntity = clientRepository.findOne(Integer.valueOf(command.getClientId()));
            if (clientEntity != null) {
                contactEntity.setClient(clientEntity);
            }
            ContactEntity savedEntity = repository.save(contactEntity);
            return new Contact(savedEntity);
        }
        return null;
    }
}
