package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.ClientEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ContactEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Benedikt Volkmer
 *         Created on 5/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientTest {

    private static Integer CLIENT_ID = 0;
    private static String CLIENT_NAME = "client name";
    private static String CLIENT_DESCRIPTION = "client description";
    private static String CLIENT_LOCATION = "client location";

    @Mock ClientEntity clientEntity;
    @Mock ContactEntity contactEntity1;
    @Mock ContactEntity contactEntity2;
    @Mock ContactEntity contactEntity3;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void entityConstructor() throws Exception {
        when(clientEntity.getId()).thenReturn(CLIENT_ID);
        when(clientEntity.getName()).thenReturn(CLIENT_NAME);
        when(clientEntity.getDescription()).thenReturn(CLIENT_DESCRIPTION);
        when(clientEntity.getLocation()).thenReturn(CLIENT_LOCATION);
        when(contactEntity1.getId()).thenReturn(0);
        when(contactEntity2.getId()).thenReturn(1);
        when(contactEntity3.getId()).thenReturn(2);
        Set<ContactEntity> contactEntities = new HashSet<>(
                Arrays.asList(contactEntity1, contactEntity2, contactEntity3));
        when(clientEntity.getContactEntities()).thenReturn(contactEntities);

        Client client = new Client(clientEntity);

        assertThat(client).as("constructed from entity")
                          .hasFieldOrPropertyWithValue("id", CLIENT_ID)
                          .hasFieldOrPropertyWithValue("name", CLIENT_NAME)
                          .hasFieldOrPropertyWithValue("description", CLIENT_DESCRIPTION)
                          .hasFieldOrPropertyWithValue("location", CLIENT_LOCATION);
        assertThat(client.getContactIdList()).contains(0, 1, 2);
    }

}