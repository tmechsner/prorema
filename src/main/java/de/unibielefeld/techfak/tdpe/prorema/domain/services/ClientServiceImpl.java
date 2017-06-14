package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewClientCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Client;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ClientEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ClientRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * ClientServiceImpl.
 */
@Service
@Primary
public class ClientServiceImpl
        extends AbstactServiceImpl<Client, ClientRepository, ClientEntity>
        implements ClientService {

    @Autowired
    public ClientServiceImpl(ClientRepository repository) {
        super(repository);
        accessDecider = AccessDeciderPool.client;
    }

    @Override
    protected Client init(ClientEntity entity) {
        return new Client(entity);
    }

    @Override
    public Client createClient(NewClientCmd command) throws PermissionDeniedException {
        if (command != null) {
            ClientEntity clientEntity;
            if (command.getId() != null && !command.getId().isEmpty()
                && repository.exists(Integer.valueOf(command.getId()))) {
                accessDecider.isAllowedThrow(Action.MODIFY, null);
                clientEntity = repository.findOne(Integer.valueOf(command.getId()));
                clientEntity.setName(command.getName());
                clientEntity.setDescription(command.getDescription());
                clientEntity.setLocation(command.getLocation());
            } else {
                accessDecider.isAllowedThrow(Action.CREATE, null);
                clientEntity = ClientEntity.builder()
                                           .name(command.getName())
                                           .description(command.getDescription())
                                           .location(command.getLocation()).build();
            }
            ClientEntity saved = repository.save(clientEntity);
            return new Client(saved);
        }
        return null;
    }
}
