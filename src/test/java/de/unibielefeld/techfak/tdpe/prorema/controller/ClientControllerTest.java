package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.ClientController;
import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewClientCmd;
import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewContactCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Client;
import de.unibielefeld.techfak.tdpe.prorema.domain.Contact;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ClientService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ContactService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.locking.DomainIdentifier;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.TestHelpers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDate;
import java.util.*;

import org.hamcrest.Matchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Benedikt Volkmer
 *         Created on 5/25/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientControllerTest {

    private static Integer CLIENT_ID = 0;
    private static String CLIENT_NAME = "client name";
    private static String CLIENT_DESCRIPTION = "client description";
    private static String CLIENT_LOCATION = "client location";
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

    private static Employee user = new Employee("Title", "Tim", "Toaster", "", "", "", "", "", "",
                                                Employee.Position.CONSULTANT, 50, LocalDate.now().minusYears(5),
                                                LocalDate.now().plusYears(3), "tim", "1234");

    @InjectMocks
    ClientController clientController;

    @Mock ClientService clientService;
    @Mock ContactService contactService;
    @Mock ProjectService projectService;
    @Mock EmployeeService employeeService;
    @Mock SimpleLockService simpleLockService;
    @Mock Client client;
    @Mock Contact contact;
    @Mock Project project;

    MockMvc mockMvc;

    @BeforeClass
    public static void setUpSecurityContext() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        user.setId(1);

        SecurityContextHolder.setContext(securityContext);

        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        TestHelpers.setCurrentLogin(user);
    }

    @Before
    public void setUp() throws PermissionDeniedException {
        MockitoAnnotations.initMocks(this);
        clientController = new ClientController(clientService, contactService, projectService, employeeService, simpleLockService);
        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setPrefix("/templates/");
        irvr.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(clientController).setViewResolvers(irvr).build();

        when(clientService.findOne(CLIENT_ID)).thenReturn(client);
        when(clientService.getAll()).thenReturn(Arrays.asList(client, client, client));
        when(clientService.createClient(any(NewClientCmd.class))).thenReturn(client);
        when(client.getId()).thenReturn(CLIENT_ID);
        when(client.getName()).thenReturn(CLIENT_NAME);
        when(client.getDescription()).thenReturn(CLIENT_DESCRIPTION);
        when(client.getLocation()).thenReturn(CLIENT_LOCATION);
        when(client.getContactIdList()).thenReturn(Arrays.asList(0, 1, 2));
        when(client.getHistory()).thenReturn(new ArrayList<ChangelogEntry>());
        when(contactService.findOne(CONTACT_ID)).thenReturn(contact);
        when(contactService.findAll(eq(Arrays.asList(0, 1, 2)))).thenReturn(Arrays.asList(contact, contact, contact));
        when(contactService.getAll()).thenReturn(Arrays.asList(contact, contact, contact));
        when(contactService.createContact(any(NewContactCmd.class))).thenReturn(contact);
        when(contact.getProjectIdList()).thenReturn(Arrays.asList(0, 1));
        when(contact.getClientId()).thenReturn(CLIENT_ID);
        when(projectService.findAll(eq(Arrays.asList(0, 1)))).thenReturn(Arrays.asList(project, project));
    }

    @Test
    public void clientProfile() throws Exception {
        Set<Project> projectSet = new LinkedHashSet<>();
        projectSet.add(project);
        //With id parameter
        mockMvc.perform(get("/client").param("id", String.valueOf(CLIENT_ID)))
               .andExpect(status().isOk())
               .andExpect(model().attribute("client", client))
               .andExpect(model().attribute("contacts", Arrays.asList(contact, contact, contact)))
               .andExpect(model().attribute("projects", projectSet))
               .andExpect(view().name("clientProfile"));

        //Without parameter
        mockMvc.perform(get("/client"))
               .andExpect(view().name("error/error"));
    }

    @Test
    public void contactProfile() throws Exception {
        Set<Project> projectSet = new LinkedHashSet<>();
        projectSet.add(project);
        //With id parameter
        mockMvc.perform(get("/contact").param("id", String.valueOf(CONTACT_ID)))
               .andExpect(status().isOk())
               .andExpect(model().attribute("contact", contact))
               .andExpect(model().attribute("projects", projectSet))
               .andExpect(view().name("contactProfile"));

        //Without parameter
        mockMvc.perform(get("/contact"))
               .andExpect(view().name("error/error"));

    }

    @Test
    public void showClientForm() throws Exception {
        NewClientCmd ncc = (NewClientCmd) mockMvc.perform(get("/clientform"))
                                                 .andExpect(status().isOk())
                                                 .andExpect(model().attribute("newClientCmd",
                                                                              Matchers.any(NewClientCmd.class)))
                                                 .andExpect(view().name("clientform"))
                                                 .andReturn().getModelAndView().getModel().get("newClientCmd");

        assertThat(ncc).isNotNull().as("no id is given")
                       .hasFieldOrPropertyWithValue("id", null)
                       .hasFieldOrPropertyWithValue("name", null)
                       .hasFieldOrPropertyWithValue("description", null)
                       .hasFieldOrPropertyWithValue("location", null);

        ncc = (NewClientCmd) mockMvc.perform(get("/clientform").param("id", String.valueOf(CLIENT_ID)))
                                    .andExpect(status().isOk())
                                    .andExpect(model().attribute("newClientCmd", Matchers.any(NewClientCmd.class)))
                                    .andExpect(view().name("clientform"))
                                    .andReturn().getModelAndView().getModel().get("newClientCmd");

        assertThat(ncc).isNotNull().as("an id is given")
                       .hasFieldOrPropertyWithValue("id", String.valueOf(CLIENT_ID))
                       .hasFieldOrPropertyWithValue("name", CLIENT_NAME)
                       .hasFieldOrPropertyWithValue("description", CLIENT_DESCRIPTION)
                       .hasFieldOrPropertyWithValue("location", CLIENT_LOCATION);
    }

    @Test
    public void saveClient() throws Exception, PermissionDeniedException {
        NewClientCmd ncc = (NewClientCmd) mockMvc.perform(post("/clientform").param("name", CLIENT_NAME)
                                                                             .param("description", CLIENT_DESCRIPTION)
                                                                             .param("location", CLIENT_LOCATION)
                                                                             .param("id", CLIENT_ID.toString()))
                                                 .andExpect(status().isFound()) // aka. 302 aka. redirected
                                                 .andExpect(model().hasNoErrors())
                                                 .andExpect(model().attribute("newClientCmd",
                                                                              Matchers.any(NewClientCmd.class)))
                                                 .andExpect(redirectedUrl("/client?id=" + CLIENT_ID.toString()))
                                                 .andReturn().getModelAndView().getModel().get("newClientCmd");
        assertThat(ncc).isNotNull().as("all params are valid")
                       .hasFieldOrPropertyWithValue("name", CLIENT_NAME)
                       .hasFieldOrPropertyWithValue("description", CLIENT_DESCRIPTION)
                       .hasFieldOrPropertyWithValue("location", CLIENT_LOCATION)
                       .hasFieldOrPropertyWithValue("id", CLIENT_ID.toString());
        //Verify client creation
        verify(clientService).createClient(ncc);
        //Verify lock release
        verify(simpleLockService).releaseLock(eq(new DomainIdentifier(CLIENT_ID, Client.class)));

        //Test input validation
        mockMvc.perform(post("/clientform").param("id", "nan")) //
               .andExpect(status().isOk())
               .andExpect(model().hasErrors())
               .andExpect(model().attribute("newClientCmd", Matchers.any(NewClientCmd.class)))
               .andExpect(model().attributeHasFieldErrors("newClientCmd", "name"))
               .andExpect(view().name("clientform"));
    }

    @Test
    public void showContactForm() throws Exception {
        NewContactCmd ncc = (NewContactCmd) mockMvc.perform(get("/contactform"))
                                                   .andExpect(status().isOk())
                                                   .andExpect(model().attribute("newContactCmd",
                                                                                Matchers.any(NewContactCmd.class)))
                                                   .andExpect(view().name("contactform"))
                                                   .andReturn().getModelAndView().getModel().get("newContactCmd");

        assertThat(ncc).isNotNull().as("no id is given")
                       .hasFieldOrPropertyWithValue("id", null);

        ncc = (NewContactCmd) mockMvc.perform(get("/contactform").param("id", String.valueOf(CONTACT_ID)))
                                     .andExpect(status().isOk())
                                     .andExpect(model().attribute("newContactCmd", Matchers.any(NewContactCmd.class)))
                                     .andExpect(view().name("contactform"))
                                     .andReturn().getModelAndView().getModel().get("newContactCmd");

        assertThat(ncc).isNotNull().as("an id is given")
                       .hasFieldOrPropertyWithValue("id", String.valueOf(CONTACT_ID));
    }

    @Test
    public void saveContact() throws Exception, PermissionDeniedException {
        NewContactCmd ncc = (NewContactCmd) mockMvc.perform(post("/contactform").param("nameTitle", CONTACT_NAME_TITLE)
                                                                                .param("firstName", CONTACT_FIRST_NAME)
                                                                                .param("lastName", CONTACT_LAST_NAME)
                                                                                .param("email", CONTACT_EMAIL)
                                                                                .param("tel", CONTACT_TEL)
                                                                                .param("street", CONTACT_STREET)
                                                                                .param("zip", CONTACT_ZIP)
                                                                                .param("city", CONTACT_CITY)
                                                                                .param("country", CONTACT_COUNTRY)
                                                                                .param("clientId", CLIENT_ID.toString())
                                                                                .param("id", CONTACT_ID.toString()))
                                                   .andExpect(status().is(302))
                                                   .andExpect(model().hasNoErrors())
                                                   .andExpect(model().attribute("newContactCmd",
                                                                                Matchers.any(NewContactCmd.class)))
                                                   .andExpect(redirectedUrl("/contact?id=" + CLIENT_ID.toString()))
                                                   .andReturn().getModelAndView()
                                                   .getModel().get("newContactCmd");
        assertThat(ncc).isNotNull().as("all params are valid")
                       .hasFieldOrPropertyWithValue("nameTitle", CONTACT_NAME_TITLE)
                       .hasFieldOrPropertyWithValue("firstName", CONTACT_FIRST_NAME)
                       .hasFieldOrPropertyWithValue("lastName", CONTACT_LAST_NAME)
                       .hasFieldOrPropertyWithValue("email", CONTACT_EMAIL)
                       .hasFieldOrPropertyWithValue("tel", CONTACT_TEL)
                       .hasFieldOrPropertyWithValue("street", CONTACT_STREET)
                       .hasFieldOrPropertyWithValue("zip", CONTACT_ZIP)
                       .hasFieldOrPropertyWithValue("city", CONTACT_CITY)
                       .hasFieldOrPropertyWithValue("country", CONTACT_COUNTRY)
                       .hasFieldOrPropertyWithValue("clientId", CLIENT_ID.toString())
                       .hasFieldOrPropertyWithValue("id", CONTACT_ID.toString());
        //Verify client creation
        verify(contactService).createContact(ncc);
        //Verify lock release
        verify(simpleLockService).releaseLock(eq(new DomainIdentifier(CONTACT_ID, Contact.class)));

        //Test input validation with clientId not a number
        mockMvc.perform(post("/contactform").param("id", "nan").param("clientId", "nan")) //
               .andExpect(status().isOk())
               .andExpect(model().hasErrors())
               .andExpect(model().attribute("newContactCmd", Matchers.any(NewContactCmd.class)))
               .andExpect(model().attributeHasFieldErrors("newContactCmd", "firstName", "lastName", "clientId"))
               .andExpect(view().name("contactform"));
        //Alternative invalid with clientId not set
        mockMvc.perform(post("/contactform").param("id", "nan")) //
               .andExpect(status().isOk())
               .andExpect(model().hasErrors())
               .andExpect(model().attribute("newContactCmd", Matchers.any(NewContactCmd.class)))
               .andExpect(model().attributeHasFieldErrors("newContactCmd", "firstName", "lastName", "clientId"))
               .andExpect(view().name("contactform"));
    }

    @Test
    public void showClientList() throws Exception {
        mockMvc.perform(get("/clientlist").param("id", String.valueOf(CONTACT_ID)))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("listFragment"))
               .andExpect(model().attributeExists("gridFragment"))
               .andExpect(model().attributeExists("domain"))
               .andExpect(model().attribute("clients", Arrays.asList(client, client, client)))
               .andExpect(view().name("list"));
    }

    //@Test
    public void showContactList() throws Exception {
        Map<Integer, String> clients = new HashMap<>();
        clients.put(CLIENT_ID, CLIENT_NAME);
        mockMvc.perform(get("/contactlist").param("id", String.valueOf(CONTACT_ID)))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("listFragment"))
               .andExpect(model().attributeExists("gridFragment"))
               .andExpect(model().attributeExists("domain"))
               .andExpect(model().attribute("contacts", Arrays.asList(contact, contact, contact)))
               .andExpect(model().attribute("clients", Matchers.equalTo(clients)))
               .andExpect(view().name("list"));

    }

}