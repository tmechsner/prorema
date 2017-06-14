package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewProjectCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.*;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Timo Mechsner on 26.04.16.
 */
@Service
@Log4j2
public class ProjectServiceImpl
        extends AbstactServiceImpl<Project, ProjectRepository, ProjectEntity>
        implements ProjectService {

    private static final String DATE_SEPARATOR = "-";
    private ContactRepository contactRepository;

    private EmployeeRepository employeeRepository;

    private SkillRepository skillRepository;

    private NeedsSkillRepository needsSkillRepository;

    private EmployeeService employeeService;

    private SkillService skillService;

    private OrganisationUnitRepository organisationUnitRepository;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, EmployeeRepository employeeRepository,
                              EmployeeService employeeService, ContactRepository contactRepository,
                              OrganisationUnitRepository organisationUnitRepository, SkillService skillService,
                              NeedsSkillRepository needsSkillRepository, SkillRepository skillRepository) {
        super(projectRepository);
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
        this.contactRepository = contactRepository;
        this.organisationUnitRepository = organisationUnitRepository;
        this.skillService = skillService;
        this.needsSkillRepository = needsSkillRepository;
        this.skillRepository = skillRepository;
        accessDecider = AccessDeciderPool.project;
    }

    @Override
    public List<Project> getAll() {
        ArrayList<Project> projects = new ArrayList<>();
        repository.findAll().forEach(projectEntity -> {
            if (projectEntity.getId() != 0) {
                projects.add(new Project(projectEntity));
            }
        });
        projects.remove(findOne(7000));
        return projects;
    }

    @Override
    public List<Project> getByOrganisationUnit(Integer id) {
        ArrayList<Project> projects = new ArrayList<>();
        repository.findByOrganisationUnit_Id(id).forEach(projectEntity -> {
            if (projectEntity.getId() != 0) {
                projects.add(new Project(projectEntity));
            }
        });
        projects.remove(findOne(7000));
        return projects;
    }

    @Override
    public List<Project> getByRunning(Boolean running) {
        ArrayList<Project> projects = new ArrayList<>();
        repository.findByRunning(running).forEach(projectEntity -> {
            if (projectEntity.getId() != 0) {
                projects.add(new Project(projectEntity));
            }
        });
        projects.remove(findOne(7000));
        return projects;
    }

    @Override
    public List<Project> getByEndDateIsPast() {
        ArrayList<Project> projects = new ArrayList<>();
        repository.findAll().forEach(projectEntity -> {
            if (projectEntity.getId() != 0) {
                if (projectEntity.getEndDate() != null && projectEntity.getEndDate().isBefore(LocalDate.now())) {
                    projects.add(new Project(projectEntity));
                }
            }
        });
        projects.remove(repository.findOne(7000));
        return projects;
    }

    @Override
    public Project create(NewProjectCmd command) throws PermissionDeniedException {

        if (command != null) {

            ProjectEntity projectEntity;

            String startDateString = command.getStartDate();
            LocalDate startDate = StringUtils.isEmpty(startDateString) ? null : LocalDate.parse(startDateString);

            String endDateString = command.getEndDate();
            LocalDate endDate = StringUtils.isEmpty(endDateString) ? null : LocalDate.parse(endDateString);

            String projectId = command.getId();
            if (command.getConversionProbability() != null) {
                if (command.getConversionProbability() == 100) {
                    command.setStatus("WON");
                }
                if (command.getConversionProbability() == 0) {
                    command.setStatus("LOST");
                }
            }
            if (!StringUtils.isEmpty(projectId)
                && repository.exists(Integer.valueOf(projectId))) {

                projectEntity = repository.findOne(Integer.valueOf(projectId));
                accessDecider.isAllowedThrow(Action.MODIFY, projectEntity);

                projectEntity.setName(command.getName());
                projectEntity.setDescription(command.getDescription());
                if (command.getStatus().equals("WON")) {
                    projectEntity.setRunning(true);
                } else {
                    if (command.getStatus().equals("LOST")) {
                        projectEntity.setRunning(false);
                    } else {
                        projectEntity.setRunning(Boolean.parseBoolean(command.getRunning()));
                    }
                }
                projectEntity.setStatus(command.getStatus());
                projectEntity.setProjectVolume(command.getPotentialProjectVolume());
                projectEntity.setConversionProbability(command.getConversionProbability());
                projectEntity.setRunning(Boolean.parseBoolean(command.getRunning()));
                projectEntity.setManDays(command.getMenDays());
                projectEntity.setStartDate(startDate);
                projectEntity.setEndDate(endDate);
                projectEntity.setCountry(command.getCountry());
                projectEntity.setCity(command.getCity());
                projectEntity.setStreet(command.getStreet());
                projectEntity.setZip(command.getZip());
            } else {
                accessDecider.isAllowedThrow(Action.CREATE, null);
                if (command.getStatus().equals("WON")) {
                    projectEntity = new ProjectEntity(command.getName(),
                                                        command.getDescription(),
                                                        command.getStatus(),
                                                        command.getPotentialProjectVolume(),
                                                        command.getConversionProbability(),
                                                        Boolean.valueOf(true),
                                                        command.getMenDays(),
                                                        startDate,
                                                        endDate,
                                                        command.getCountry(),
                                                        command.getCity(),
                                                        command.getStreet(),
                                                        command.getZip());
                } else {
                    if (command.getStatus().equals("LOST")) {
                        projectEntity = new ProjectEntity(command.getName(),
                                                            command.getDescription(),
                                                            command.getStatus(),
                                                            command.getPotentialProjectVolume(),
                                                            command.getConversionProbability(),
                                                            Boolean.valueOf(false),
                                                            command.getMenDays(),
                                                            startDate,
                                                            endDate,
                                                            command.getCountry(),
                                                            command.getCity(),
                                                            command.getStreet(),
                                                            command.getZip());
                    } else {
                        projectEntity = new ProjectEntity(command.getName(),
                                                            command.getDescription(),
                                                            command.getStatus(),
                                                            command.getPotentialProjectVolume(),
                                                            command.getConversionProbability(),
                                                            Boolean.valueOf(command.getRunning()),
                                                            command.getMenDays(),
                                                            startDate,
                                                            endDate,
                                                            command.getCountry(),
                                                            command.getCity(),
                                                            command.getStreet(),
                                                            command.getZip());
                    }
                }
            }
            String contactId = command.getContactId();
            if (!StringUtils.isEmpty(contactId)) {
                projectEntity.setContact(contactRepository.findOne(Integer.valueOf(contactId)));
            }

            String projectManagerId = command.getProjectManagerId();
            if (!StringUtils.isEmpty(projectManagerId)) {
                projectEntity.setProjectManager(employeeRepository.findOne(Integer.valueOf(projectManagerId)));
            }

            String orgaUnitId = command.getOrgaUnitId();
            if (!StringUtils.isEmpty(orgaUnitId)) {
                projectEntity.setOrganisationUnit(organisationUnitRepository.findOne(Integer.parseInt(orgaUnitId)));
            }

            ProjectEntity savedEntity = repository.save(projectEntity);
            return new Project(savedEntity);
        }
        return null;
    }

    /**
     * Converts a project form not running status.
     * @param project
     * @return
     * @throws PermissionDeniedException
     */
    public Project convert(Project project) throws PermissionDeniedException {
        if (project != null) {
            if (project.getRunning() == Boolean.FALSE) {
                ProjectEntity projectEntity = repository.findOne(project.getId());

                projectEntity.setRunning(Boolean.TRUE);
                projectEntity.setStatus("WON");
                projectEntity.setConversionProbability(100);

                ProjectEntity savedEntity = repository.save(projectEntity);
                return new Project(savedEntity);
            } else {
                return project;
            }
        } else {
            return project;
        }

    }


    public EmployeeService getEmployeeService() {
        return employeeService;
    }

    @Override
    protected Project init(ProjectEntity entity) {
        if (entity != null) {
            return new Project(entity);
        } else {
            return null;
        }
    }
}
