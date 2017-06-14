package de.unibielefeld.techfak.tdpe.prorema.controller;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewClientCmd;
import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewContactCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Client;
import de.unibielefeld.techfak.tdpe.prorema.domain.Contact;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.locking.DomainIdentifier;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ClientService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ContactService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.ProjectService;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.*;

/**
 * Controller for Client and its Contacts based content.
 * Created by Benedikt Volkmer on 5/6/16.
 */
@Controller
@Log4j2
public class ClientController extends ErrorHandler {
    private static final String CLIENT = "client";
    private static final String PROJECTS = "projects";
    private static final String CLIENTFORM = "clientform";
    private static final String CONTACTFORM = "contactform";
    private static final String CONTACT = "contact";
    private static final String CONTACTS = "contacts";
    private static final String CLIENTS = "clients";
    private static final String LIST_FRAGMENT = "listFragment";
    private static final String GRID_FRAGMENT = "gridFragment";
    private static final String DOMAIN = "domain";
    private static final String LIST = "list";

    private final ClientService clientService;
    private final ContactService contactService;
    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final SimpleLockService simpleLockService;

    /**
     * @param clientService     ClientService autowired
     * @param contactService    ContactService autowired
     * @param projectService    ProjectService autowired
     * @param simpleLockService SimpleLockService autowired
     */
    @Autowired
    public ClientController(ClientService clientService, ContactService contactService, ProjectService projectService,
                            EmployeeService employeeService, SimpleLockService simpleLockService) {
        this.clientService = clientService;
        this.contactService = contactService;
        this.projectService = projectService;
        this.employeeService = employeeService;
        this.simpleLockService = simpleLockService;
    }

    /**
     * Mapping for client profiles.
     *
     * @param id    Id of this client
     * @param model model provided to the content
     * @return client profile page
     */
    @RequestMapping(value = "/client", method = RequestMethod.GET)
    public String clientProfile(@RequestParam(name = "id") int id, Model model) {
        try {
            Client client = clientService.findOne(id);
            if(client == null) {
                log.error("Client with id " + id + " does not exist.");
                model.addAttribute("error", "Klient mit der id " + id + " existiert nicht!");
                return "clientProfile";
            }
            model.addAttribute(CLIENT, client);
            List<Contact> contacts = contactService.findAll(client.getContactIdList());
            model.addAttribute(CONTACTS, contacts);
            Set<Project> projects = new LinkedHashSet<>();
            contacts.forEach(contact -> projects.addAll(projectService.findAll(contact.getProjectIdList())));
            model.addAttribute(PROJECTS, projects);
            model.addAttribute("position", LoginInfo.getPosition().toString());

            List<ChangelogEntry> changelogs = client.getHistory();
            Map<Integer, Employee> users = new HashMap<>();
            changelogs.forEach(changelogEntry -> {
                users.put(changelogEntry.getChangelogEntryId(), employeeService.findOne(changelogEntry.getUserId()));
            });
            model.addAttribute("changelogs", changelogs);
            model.addAttribute("users", users);

        } catch (Exception e) {
            log.error("Error showing client profile.", e);
            throw e;
        }
        return "clientProfile";
    }

    /**
     * Mapping for contact profiles.
     *
     * @param id    Id of this contact
     * @param model model provided to the content
     * @return content profile page
     */
    @RequestMapping(value = "/contact", method = RequestMethod.GET)
    public String contactProfile(@RequestParam(name = "id") int id, Model model) {
        try {
            Contact contact = contactService.findOne(id);
            if(contact != null ) {
                model.addAttribute(CONTACT, contact);
                model.addAttribute(CLIENT, clientService.findOne(contact.getClientId()));
                Set<Project> projectSet = new HashSet<>();
                projectSet.addAll(projectService.findAll(contact.getProjectIdList()));
                model.addAttribute(PROJECTS, projectSet);
                model.addAttribute("position", LoginInfo.getPosition().toString());

                List<ChangelogEntry> changelogs = contact.getHistory();
                Map<Integer, Employee> users = new HashMap<>();
                changelogs.forEach(changelogEntry -> {
                    users.put(changelogEntry.getChangelogEntryId(), employeeService.findOne(changelogEntry.getUserId()));
                });
                model.addAttribute("changelogs", changelogs);
                model.addAttribute("users", users);
            } else {
                log.error("Contact with ID " + id + " not found!");
                model.addAttribute("error", "Der Kontakt mit der ID " + id + " wurde nicht gefunden!");
            }
        } catch (Exception e) {
            log.error("Error showing contact profile.", e);
            throw e;
        }
        return "contactProfile";
    }

    /**
     * Mapping of client form.
     *
     * @param newClientCmd command to fill with
     * @param model        Modell
     * @param id           optional client id
     * @return clientform
     */
    @RequestMapping(value = "/clientform", method = RequestMethod.GET)
    public String showClientForm(NewClientCmd newClientCmd, Model model,
                                 @RequestParam(value = "id", required = false) String id) {
        try {
            if (id != null) {
                Client client = clientService.findOne(Integer.valueOf(id));
                if (client != null) {
                    newClientCmd.setId(id);
                    newClientCmd.setName(client.getName());
                    newClientCmd.setDescription(client.getDescription());
                    newClientCmd.setLocation(client.getLocation());
                    model.addAttribute(newClientCmd);
                }
            }
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing client form.", e);
            throw e;
        }
        return CLIENTFORM;
    }

    /**
     * Client Form evaluation.
     *
     * @param newClientCmd  filled command
     * @param bindingResult generated
     * @param model         model
     * @return clientform if input is invalid, redirect to client profile if successive.
     */
    @RequestMapping(value = "/clientform", method = RequestMethod.POST)
    public String saveClient(@Valid NewClientCmd newClientCmd, BindingResult bindingResult, Model model) throws
                                                                                                         PermissionDeniedException {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("newClientCmd", newClientCmd);
                model.addAttribute("position", LoginInfo.getPosition().toString());
                log.debug("Client form input not valid.");
                return CLIENTFORM;
            } else {
                Client client = clientService.createClient(newClientCmd);
                simpleLockService.releaseLock(new DomainIdentifier(client.getId(), Client.class));
                return "redirect:/client?id=" + client.getId();
            }
        } catch (Exception e) {
            log.error("Error evaluating client form.", e);
            throw e;
        }
    }

    /**
     * Mapping for a contact form.
     *
     * @param newContactCmd command to fill with
     * @param model         Modell
     * @param clientId      client id to add contact to
     * @param id            id of contact to modify
     * @return contactform
     */
    @RequestMapping(value = "/contactform", method = RequestMethod.GET)
    public String showContactForm(NewContactCmd newContactCmd, Model model,
                                  @RequestParam(value = "clientid", required = false) @NumberFormat String clientId,
                                  @RequestParam(value = "id", required = false) @NumberFormat String id) {
        try {
            if (id != null) {
                Contact contact = contactService.findOne(Integer.valueOf(id));
                if (contact != null) {
                    newContactCmd.setId(id);
                    newContactCmd.setNameTitle(contact.getNameTitle());
                    newContactCmd.setFirstName(contact.getFirstName());
                    newContactCmd.setLastName(contact.getLastName());
                    newContactCmd.setEmail(contact.getMail());
                    newContactCmd.setTel(contact.getTel());
                    newContactCmd.setStreet(contact.getStreet());
                    newContactCmd.setZip(contact.getZip());
                    newContactCmd.setCity(contact.getCity());
                    newContactCmd.setCountry(contact.getCountry());
                    newContactCmd.setClientId(contact.getClientId().toString());
                }

            }
            if(clientId != null && !clientId.isEmpty()) {
                newContactCmd.setClientId(clientId);
            }
            model.addAttribute("position", LoginInfo.getPosition().toString());
            model.addAttribute(CLIENTS, clientService.getAll());

        } catch (Exception e) {
            log.error("Error showing contact form.", e);
            throw e;
        }
        return CONTACTFORM;
    }

    /**
     * Contact form evaluation.
     *
     * @param newContactCmd command filled with values
     * @param bindingResult generated
     * @param model         Modell
     * @return contact form if input invalid, redirect to contact profile if successive
     */
    @RequestMapping(value = "/contactform", method = RequestMethod.POST)
    public String saveContact(@Valid NewContactCmd newContactCmd, BindingResult bindingResult, Model model) throws
                                                                                                            PermissionDeniedException {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("newContactCmd", newContactCmd);
                model.addAttribute(CLIENTS, clientService.getAll());
                model.addAttribute("position", LoginInfo.getPosition().toString());
                log.debug("New contact form input not valid.");
                return CONTACTFORM;
            } else {
                Contact contact = contactService.createContact(newContactCmd);
                simpleLockService.releaseLock(new DomainIdentifier(contact.getId(), Contact.class));
                return "redirect:/contact?id=" + contact.getId();
            }
        } catch (Exception e) {
            log.error("Error evaluating contact form.", e);
            throw e;
        }
    }

    /**
     * Clientlist mapping.
     *
     * @param model Modell
     * @return clientlist
     */
    @RequestMapping("/clientlist")
    public String showClientList(Model model) {
        try {
            // List specific attributes
            model.addAttribute(LIST_FRAGMENT, "clientList");
            model.addAttribute(GRID_FRAGMENT, "clientGrid");
            model.addAttribute(DOMAIN, CLIENT);

            //Domain/fragment specific attributes
            model.addAttribute(CLIENTS, clientService.getAll());
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing client list.", e);
            throw e;
        }

        return LIST;
    }

    /**
     * Contactlist mapping.
     *
     * @param model Modell
     * @return contactlist
     */
    @RequestMapping("/contactlist")
    public String showContactList(Model model) {
        try {
            // List specific attributes
            model.addAttribute(LIST_FRAGMENT, "contactList");
            model.addAttribute(GRID_FRAGMENT, "contactGrid");
            model.addAttribute(DOMAIN, CONTACT);

            //Domain/fragment specific attributes
            List<Contact> contacts = contactService.getAll();
            model.addAttribute(CONTACTS, contacts);
            Map<Integer, String> clients = new HashMap();
            contacts.parallelStream()
                    .map(contact -> clientService.findOne(contact.getClientId()))
                    .forEach(client -> clients.putIfAbsent(client.getId(), client.getName()));
            model.addAttribute(CLIENTS, clients);
            model.addAttribute("position", LoginInfo.getPosition().toString());
        } catch (Exception e) {
            log.error("Error showing contact list.", e);
            throw e;
        }

        return LIST;
    }
}
