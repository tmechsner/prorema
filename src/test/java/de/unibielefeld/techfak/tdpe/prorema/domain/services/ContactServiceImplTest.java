package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewContactCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Contact;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ClientEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ContactEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ClientRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ContactRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Benedikt Volkmer
 *         Created on 5/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContactServiceImplTest {

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

    private ContactServiceImpl contactService;

    @Mock private ContactRepository contactRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private NewContactCmd newContactCmd;
    @Mock private ContactEntity contactEntity;
    @Mock private ClientEntity clientEntity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        contactService = new ContactServiceImpl(contactRepository, clientRepository);

        when(newContactCmd.getNameTitle()).thenReturn(CONTACT_NAME_TITLE);
        when(newContactCmd.getFirstName()).thenReturn(CONTACT_FIRST_NAME);
        when(newContactCmd.getLastName()).thenReturn(CONTACT_LAST_NAME);
        when(newContactCmd.getEmail()).thenReturn(CONTACT_EMAIL);
        when(newContactCmd.getTel()).thenReturn(CONTACT_TEL);
        when(newContactCmd.getStreet()).thenReturn(CONTACT_STREET);
        when(newContactCmd.getZip()).thenReturn(CONTACT_ZIP);
        when(newContactCmd.getCity()).thenReturn(CONTACT_CITY);
        when(newContactCmd.getCountry()).thenReturn(CONTACT_COUNTRY);
        when(newContactCmd.getClientId()).thenReturn(CLIENT_ID.toString());
        when(clientEntity.getId()).thenReturn(CLIENT_ID);
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
        when(contactRepository.findOne(CONTACT_ID)).thenReturn(contactEntity);
        when(clientRepository.findOne(CLIENT_ID)).thenReturn(clientEntity);
    }

    @Test
    public void createContact() throws Exception, PermissionDeniedException {
        // Test with existing id
        when(newContactCmd.getId()).thenReturn(CONTACT_ID.toString());
        when(contactRepository.exists(CONTACT_ID)).thenReturn(true);
        when(contactRepository.save(contactEntity)).thenReturn(contactEntity);

        Contact contact = contactService.createContact(newContactCmd);

        verify(newContactCmd, atLeastOnce()).getId();
        verify(clientRepository).findOne(CONTACT_ID);
        verify(contactEntity).setNameTitle(CONTACT_NAME_TITLE);
        verify(contactEntity).setFirstName(CONTACT_FIRST_NAME);
        verify(contactEntity).setLastName(CONTACT_LAST_NAME);
        verify(contactEntity).setEmail(CONTACT_EMAIL);
        verify(contactEntity).setTel(CONTACT_TEL);
        verify(contactEntity).setStreet(CONTACT_STREET);
        verify(contactEntity).setZip(CONTACT_ZIP);
        verify(contactEntity).setCity(CONTACT_CITY);
        verify(contactEntity).setCountry(CONTACT_COUNTRY);
        verify(clientRepository).findOne(CLIENT_ID);

        assertThat(contact).isNotNull().as("existing id is given")
                           .hasFieldOrPropertyWithValue("id", CONTACT_ID)
                           .hasFieldOrPropertyWithValue("nameTitle", CONTACT_NAME_TITLE)
                           .hasFieldOrPropertyWithValue("firstName", CONTACT_FIRST_NAME)
                           .hasFieldOrPropertyWithValue("lastName", CONTACT_LAST_NAME)
                           .hasFieldOrPropertyWithValue("mail", CONTACT_EMAIL)
                           .hasFieldOrPropertyWithValue("tel", CONTACT_TEL)
                           .hasFieldOrPropertyWithValue("street", CONTACT_STREET)
                           .hasFieldOrPropertyWithValue("zip", CONTACT_ZIP)
                           .hasFieldOrPropertyWithValue("city", CONTACT_CITY)
                           .hasFieldOrPropertyWithValue("country", CONTACT_COUNTRY);

        //Test with non existing id
        when(newContactCmd.getId()).thenReturn(CONTACT_ID.toString());
        when(clientRepository.exists(CONTACT_ID)).thenReturn(false);
        when(contactRepository.save(any(ContactEntity.class))).then((Answer<ContactEntity>) invocation -> {
            ContactEntity ret = (ContactEntity) invocation.getArguments()[0];
            ret.setId(CONTACT_ID);
            return ret;
        });

        contact = contactService.createContact(newContactCmd);

        verify(newContactCmd, atLeastOnce()).getId();
        verify(clientRepository, atLeastOnce()).findOne(CLIENT_ID);
        assertThat(contact).isNotNull().as("not existing id is given")
                           .hasFieldOrPropertyWithValue("id", CONTACT_ID)
                           .hasFieldOrPropertyWithValue("nameTitle", CONTACT_NAME_TITLE)
                           .hasFieldOrPropertyWithValue("firstName", CONTACT_FIRST_NAME)
                           .hasFieldOrPropertyWithValue("lastName", CONTACT_LAST_NAME)
                           .hasFieldOrPropertyWithValue("mail", CONTACT_EMAIL)
                           .hasFieldOrPropertyWithValue("tel", CONTACT_TEL)
                           .hasFieldOrPropertyWithValue("street", CONTACT_STREET)
                           .hasFieldOrPropertyWithValue("zip", CONTACT_ZIP)
                           .hasFieldOrPropertyWithValue("city", CONTACT_CITY)
                           .hasFieldOrPropertyWithValue("country", CONTACT_COUNTRY);

        //Test without id
        when(newContactCmd.getId()).thenReturn("");

        contact = contactService.createContact(newContactCmd);

        verify(newContactCmd, atLeastOnce()).getId();
        verify(clientRepository, atLeastOnce()).findOne(CLIENT_ID);
        assertThat(contact).isNotNull().as("no id is given")
                           .hasFieldOrPropertyWithValue("id", CONTACT_ID)
                           .hasFieldOrPropertyWithValue("nameTitle", CONTACT_NAME_TITLE)
                           .hasFieldOrPropertyWithValue("firstName", CONTACT_FIRST_NAME)
                           .hasFieldOrPropertyWithValue("lastName", CONTACT_LAST_NAME)
                           .hasFieldOrPropertyWithValue("mail", CONTACT_EMAIL)
                           .hasFieldOrPropertyWithValue("tel", CONTACT_TEL)
                           .hasFieldOrPropertyWithValue("street", CONTACT_STREET)
                           .hasFieldOrPropertyWithValue("zip", CONTACT_ZIP)
                           .hasFieldOrPropertyWithValue("city", CONTACT_CITY)
                           .hasFieldOrPropertyWithValue("country", CONTACT_COUNTRY);

    }

}