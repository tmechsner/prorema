package de.unibielefeld.techfak.tdpe.prorema.domain;

import de.unibielefeld.techfak.tdpe.prorema.persistence.ClientEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ContactEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Benedikt Volkmer
 *         Created on 5/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContactTest {

    private static Integer CONTACT_ID = 0;
    private static String CONTACT_NAME_TITLE = "contact_name_title";
    private static String CONTACT_FIRST_NAME = "contact_first_name";
    private static String CONTACT_LAST_NAME = "contact_last_name";
    private static String CONTACT_EMAIL = "contact_email";
    private static String CONTACT_TEL = "contact_tel";
    private static String CONTACT_STREET = "contact_street";
    private static String CONTACT_ZIP = "contact_zip";
    private static String CONTACT_CITY = "contact_city";
    private static String CONTACT_COUNTRY = "contact_country";
    private static Integer CLIENT_ID = 0;

    @Mock ContactEntity contactEntity;
    @Mock ClientEntity clientEntity;
    @Mock ProjectEntity projectEntity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void entityConstructor() throws Exception {
        when(contactEntity.getId()).thenReturn(CONTACT_ID);
        when(contactEntity.getNameTitle()).thenReturn(CONTACT_NAME_TITLE);
        when(contactEntity.getFirstName()).thenReturn(CONTACT_FIRST_NAME);
        when(contactEntity.getLastName()).thenReturn(CONTACT_LAST_NAME);
        when(contactEntity.getEmail()).thenReturn(CONTACT_EMAIL);
        when(contactEntity.getTel()).thenReturn(CONTACT_TEL);
        when(contactEntity.getStreet()).thenReturn(CONTACT_STREET);
        when(contactEntity.getZip()).thenReturn(CONTACT_ZIP);
        when(contactEntity.getCity()).thenReturn(CONTACT_CITY);
        when(contactEntity.getCountry()).thenReturn(CONTACT_COUNTRY);
        when(contactEntity.getClient()).thenReturn(clientEntity);
        when(contactEntity.getProjects()).thenReturn(Arrays.asList(projectEntity, projectEntity, projectEntity));
        when(clientEntity.getId()).thenReturn(CLIENT_ID);
        when(projectEntity.getId()).thenReturn(0, 1, 2);

        Contact contact = new Contact(contactEntity);

        assertThat(contact).isNotNull().as("constructed from entity")
                           .hasFieldOrPropertyWithValue("id", CONTACT_ID)
                           .hasFieldOrPropertyWithValue("nameTitle", CONTACT_NAME_TITLE)
                           .hasFieldOrPropertyWithValue("firstName", CONTACT_FIRST_NAME)
                           .hasFieldOrPropertyWithValue("lastName", CONTACT_LAST_NAME)
                           .hasFieldOrPropertyWithValue("mail", CONTACT_EMAIL)
                           .hasFieldOrPropertyWithValue("tel", CONTACT_TEL)
                           .hasFieldOrPropertyWithValue("street", CONTACT_STREET)
                           .hasFieldOrPropertyWithValue("zip", CONTACT_ZIP)
                           .hasFieldOrPropertyWithValue("city", CONTACT_CITY)
                           .hasFieldOrPropertyWithValue("country", CONTACT_COUNTRY)
                           .hasFieldOrPropertyWithValue("clientId", CLIENT_ID);
        assertThat(contact.getProjectIdList()).contains(0, 1, 2);
    }

}