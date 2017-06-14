package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewClientCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Client;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ClientEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ClientRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Benedikt Volkmer
 *         Created on 5/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientServiceImplTest {

    private static Integer CLIENT_ID = 0;
    private static String CLIENT_NAME = "client name";
    private static String CLIENT_DESCRIPTION = "client description";
    private static String CLIENT_LOCATION = "client location";

    private ClientServiceImpl clientServiceImpl;

    @Mock ClientRepository clientRepository;
    @Mock NewClientCmd newClientCmd;
    @Mock ClientEntity clientEntity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.clientServiceImpl = new ClientServiceImpl(clientRepository);

        when(newClientCmd.getName()).thenReturn(CLIENT_NAME);
        when(newClientCmd.getDescription()).thenReturn(CLIENT_DESCRIPTION);
        when(newClientCmd.getLocation()).thenReturn(CLIENT_LOCATION);
        when(clientEntity.getId()).thenReturn(CLIENT_ID);
        when(clientEntity.getName()).thenReturn(CLIENT_NAME);
        when(clientEntity.getDescription()).thenReturn(CLIENT_DESCRIPTION);
        when(clientEntity.getLocation()).thenReturn(CLIENT_LOCATION);
        when(clientRepository.findOne(CLIENT_ID)).thenReturn(clientEntity);
    }

    @Test
    public void createClient() throws Exception, PermissionDeniedException {
        // Test with existing id
        when(newClientCmd.getId()).thenReturn(CLIENT_ID.toString());
        when(clientRepository.exists(CLIENT_ID)).thenReturn(true);
        when(clientRepository.save(clientEntity)).thenReturn(clientEntity);

        Client client = clientServiceImpl.createClient(newClientCmd);

        verify(newClientCmd, atLeastOnce()).getId();
        verify(clientRepository).findOne(CLIENT_ID);
        verify(clientEntity).setName(CLIENT_NAME);
        verify(clientEntity).setDescription(CLIENT_DESCRIPTION);
        verify(clientEntity).setLocation(CLIENT_LOCATION);

        assertThat(client).isNotNull().as("existing id is given")
                          .hasFieldOrPropertyWithValue("id", CLIENT_ID)
                          .hasFieldOrPropertyWithValue("name", CLIENT_NAME)
                          .hasFieldOrPropertyWithValue("description", CLIENT_DESCRIPTION)
                          .hasFieldOrPropertyWithValue("location", CLIENT_LOCATION);

        //Test with non existing id
        when(newClientCmd.getId()).thenReturn(CLIENT_ID.toString());
        when(clientRepository.exists(CLIENT_ID)).thenReturn(false);
        when(clientRepository.save(any(ClientEntity.class))).then((Answer<ClientEntity>) invocation -> {
            ClientEntity ret = (ClientEntity) invocation.getArguments()[0];
            ret.setId(CLIENT_ID);
            return ret;
        });

        client = clientServiceImpl.createClient(newClientCmd);

        verify(newClientCmd, atLeastOnce()).getId();
        assertThat(client).isNotNull().as("existing id is given")
                          .hasFieldOrPropertyWithValue("id", CLIENT_ID)
                          .hasFieldOrPropertyWithValue("name", CLIENT_NAME)
                          .hasFieldOrPropertyWithValue("description", CLIENT_DESCRIPTION)
                          .hasFieldOrPropertyWithValue("location", CLIENT_LOCATION);

        //Test without id
        when(newClientCmd.getId()).thenReturn("");
        when(clientRepository.exists(CLIENT_ID)).thenReturn(false);

        client = clientServiceImpl.createClient(newClientCmd);

        verify(newClientCmd, atLeastOnce()).getId();
        assertThat(client).isNotNull().as("existing id is given")
                          .hasFieldOrPropertyWithValue("id", CLIENT_ID)
                          .hasFieldOrPropertyWithValue("name", CLIENT_NAME)
                          .hasFieldOrPropertyWithValue("description", CLIENT_DESCRIPTION)
                          .hasFieldOrPropertyWithValue("location", CLIENT_LOCATION);
    }

}