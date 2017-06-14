package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewEmployeeCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.HasSkillRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.OrganisationUnitRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.SkillRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.BasicSecurityConfig;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Benedikt Volkmer on 4/18/16.
 */
@Service
@Primary
@Log4j2
public class EmployeeServiceImpl
        extends AbstactServiceImpl<Employee, EmployeeRepository, EmployeeEntity>
        implements EmployeeService {

    private final HasSkillRepository hasSkillRepository;
    private final SkillRepository skillRepository;
    private final OrganisationUnitRepository organisationUnitRepository;
    private final SkillService skillService;
    private final EmployeeProfileService employeeProfileService;

    /**
     * @param employeeRepository         EmployeeRepository
     * @param hasSkillRepository         HasSkillRepository
     * @param skillRepository            SkillRepository
     * @param organisationUnitRepository OrganisationUnitRepository
     * @param skillService               SkillService
     * @param employeeProfileService     autowired
     */
    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, HasSkillRepository hasSkillRepository,
                               OrganisationUnitRepository organisationUnitRepository, SkillRepository skillRepository,
                               SkillService skillService, EmployeeProfileService employeeProfileService) {
        super(employeeRepository);
        this.hasSkillRepository = hasSkillRepository;
        this.skillRepository = skillRepository;
        this.organisationUnitRepository = organisationUnitRepository;
        this.skillService = skillService;
        this.employeeProfileService = employeeProfileService;
        accessDecider = AccessDeciderPool.employee;
    }

    @Override
    public List<Employee> getAllExceptAdmins() {
        ArrayList<Employee> employees = new ArrayList<>();
        repository.findAll().forEach(employeeEntity -> {
            if (accessDecider.isAllowed(Action.VIEW, employeeEntity)
                && !(Employee.Position.ADMINISTRATOR.toString().equalsIgnoreCase(employeeEntity.getPosition()))) {
                employees.add(new Employee(employeeEntity));
            }
        });
        return employees;
    }

    @Override
    public List<Employee> getAllNonExitedNonAdmin() {
        List<Employee> employees = getAllExceptAdmins();
        List<Employee> formerEmployees = getAllFormerEmployees();
        employees.removeAll(formerEmployees);
        return employees;
    }

    @Override
    public List<Employee> getAllNonExited() {
        List<Employee> employees = getAll();
        employees.removeAll(getAllFormerEmployees());
        return employees;
    }

    @Override
    protected Employee init(EmployeeEntity entity) {
        return new Employee(entity);
    }

    @Override
    public List<Employee> getByOrganisationUnit(Integer id) {
        ArrayList<Employee> employees = new ArrayList<>();
        repository.findByOrganisationUnit_Id(id).forEach(employeeEntity -> {
            if (accessDecider.isAllowed(Action.VIEW, employeeEntity)) {
                employees.add(new Employee(employeeEntity));
            }
        });
        return employees;
    }

    @Override
    public List<Employee> getByPosition(String position) {
        ArrayList<Employee> employees = new ArrayList<>();
        repository.findByPosition(position).forEach(employeeEntity -> {
            if (accessDecider.isAllowed(Action.VIEW, employeeEntity)) {
                employees.add(new Employee(employeeEntity));
            }
        });
        return employees;
    }


    public List<Employee> getAllFormerEmployees() {
        ArrayList<Employee> employees = new ArrayList<>();
        repository.findAll().forEach(employeeEntity -> {
            if(employeeEntity.getId() != null) {
                if(employeeEntity.getWorkExit().isBefore(LocalDate.now())) {
                    employees.add(new Employee(employeeEntity));
                }
            }
        });
        return employees;
    }



    public static boolean checkPositionChange(EmployeeEntity entity, NewEmployeeCmd command) {
        try {
            int actPrio = LoginInfo.getPosition().getPriority();
            int prioIs = Employee.Position.fromString(entity.getPosition()).getPriority();
            Employee.Position posShall = Employee.Position.fromString(command.getPosition());
            int prioShall = posShall.getPriority();
            if (prioIs > actPrio) {
                return false;
            }
            if (actPrio >= prioShall &&
                AccessDeciderPool.positionChange.isAllowed(Action.MODIFY,posShall)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public Employee create(NewEmployeeCmd command) throws PermissionDeniedException {
        if (command != null) {
            String startDateString = command.getWorkEntry();
            LocalDate workEntry = StringUtils.isEmpty(startDateString) ? null : LocalDate.parse(startDateString);
            String endDateString = command.getWorkExit();
            LocalDate workExit = StringUtils.isEmpty(endDateString) ? null : LocalDate.parse(endDateString);

            OrganisationUnitEntity organisationUnitEntity = organisationUnitRepository
                    .findOne(Integer.valueOf(command.getOrgaUnitId()));


            EmployeeEntity entity;
            if (command.getId() != null && !command.getId().isEmpty()
                && repository.exists(Integer.valueOf(command.getId()))) {
                entity = repository.findOne(Integer.valueOf(command.getId()));
                accessDecider.isAllowedThrow(Action.MODIFY, entity);
                entity.setNameTitle(command.getNameTitle());
                entity.setFirstName(command.getFirstName());
                entity.setLastName(command.getLastName());

                if (checkPositionChange(entity, command)) {
                    entity.setPosition(command.getPosition());
                }
                entity.setEmail(command.getEMail());
                entity.setTel(command.getTel());
                entity.setStreet(command.getStreet());
                entity.setZip(command.getZip());
                entity.setCity(command.getCity());
                entity.setCountry(command.getCountry());
                entity.setWorkEntry(workEntry);
                entity.setWorkExit(workExit);
                entity.setWorkschedule(command.getWorkSchedule());
                entity.setUsername(command.getUsername());
                if (!command.getPassword().equals("")) {
                    entity.setPassword((new BasicSecurityConfig()).encodePassword(command.getPassword()));
                }
                if (organisationUnitEntity != null) {
                    entity.setOrganisationUnit(organisationUnitEntity);
                }
                Set<EmployeeProfileEntity> employeeProfileEntities = entity.getEmployeeProfiles();
                command.getRemovedProfiles().stream().map(Integer::valueOf).forEach(integer -> {
                    employeeProfileService.delete(integer);
                    employeeProfileEntities.removeIf(employeeProfileEntity -> {
                        Integer id = employeeProfileEntity.getId();
                        Boolean result = id.equals(integer);
                        return result;
                    });

                });
                entity.setEmployeeProfiles(employeeProfileEntities);
            } else {
                accessDecider.isAllowedThrow(Action.CREATE, null);
                if (!AccessDeciderPool.positionChange.isAllowed(Action.CREATE, Employee.Position.fromString(command.getPosition()))) {
                    command.setPosition(Employee.Position.CONSULTANT.toString());
                }
                entity = new EmployeeEntity(command.getNameTitle(),
                                            command.getFirstName(),
                                            command.getLastName(),
                                            command.getPosition(),
                                            command.getEMail(),
                                            command.getTel(),
                                            workEntry,
                                            workExit,
                                            command.getStreet(),
                                            command.getZip(),
                                            command.getCity(),
                                            command.getCountry(),
                                            command.getWorkSchedule(),
                                            command.getUsername(),
                                            (new BasicSecurityConfig()).encodePassword(command.getPassword()),
                                            organisationUnitEntity);
            }

            final EmployeeEntity savedEntity = repository.save(entity);

            command.getNewProfiles().forEach(s -> employeeProfileService.createProfile(savedEntity, s));

            return new Employee(savedEntity);
        }
        return null;
    }
}

