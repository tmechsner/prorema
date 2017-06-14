package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewWorksOnCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOnDetails;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.WorksOnEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.ProjectRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.WorksOnRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by David on 12.05.2016.
 */
@Service
@Primary
@Log4j2
public class WorksOnServiceImpl extends AbstactServiceImpl<WorksOn, WorksOnRepository, WorksOnEntity>
        implements WorksOnService {

    private EmployeeRepository employeeRepo;
    private ProjectRepository projectRepo;

    /**
     * Constructor.
     *
     * @param worksOnRepository autowired
     * @param employeeRepo autowired
     * @param projectRepo autowired
     */
    @Autowired
    public WorksOnServiceImpl(WorksOnRepository worksOnRepository, EmployeeRepository employeeRepo,
                              ProjectRepository projectRepo) {
        super(worksOnRepository);
        accessDecider = AccessDeciderPool.worksOn;
        this.employeeRepo = employeeRepo;
        this.projectRepo = projectRepo;
    }

    @Override
    public List<WorksOn> getByDateAndEmployee(LocalDate start, LocalDate end, int emId) {
        return repository
                .findByEmployee_IdAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(emId, start, end)
                .parallelStream().map(this::init).collect(Collectors.toList());
    }

    @Override
    public List<WorksOn> getByStatus(String status) {
        return repository.findByStatus(status).parallelStream().map(this::init).collect(Collectors.toList());
    }

    @Override
    protected WorksOn init(WorksOnEntity entity) {
        return new WorksOn(entity);
    }

    @Override
    public List<WorksOn> getWorksOnByEmployee(int employeeId) {
        return repository.findByEmployee_Id(employeeId).parallelStream().map(this::init).collect(Collectors.toList());
    }

    @Override
    public List<WorksOn> getWorksOnByProject(int projectId) {
        return repository.findByProject_Id(projectId).parallelStream().map(this::init).collect(Collectors.toList());
    }

    @Override
    public List<WorksOn> createWorksOn(NewWorksOnCmd command) {
        if (command != null) {
            List<WorksOn> result = new ArrayList<>();
            for (int employeeId : command.getEmployeeIds()) {
                WorksOnEntity entity;
                ProjectEntity projectEntity = projectRepo.findOne(command.getProjectId());
                EmployeeEntity employeeEntity = employeeRepo.findOne(employeeId);
                if (projectEntity != null && employeeEntity != null) {
                    if (command.getId() != null && repository.exists(command.getId())) {
                        WorksOnDetails editedDetails = command.getWorkDetails().get(0);
                        entity = repository.findOne(Integer.valueOf(command.getId()));
                        entity.setProject(projectEntity);
                        entity.setEmployee(employeeEntity);
                        entity.setStatus(editedDetails.getStatus());
                        entity.setWorkload(editedDetails.getWorkload());
                        entity.setStartDate(LocalDate.parse(editedDetails.getStartDate()));
                        entity.setEndDate(LocalDate.parse(editedDetails.getEndDate()));

                        final WorksOnEntity edited = repository.save(entity);
                        result.add(new WorksOn(edited));
                        command.getWorkDetails().remove(editedDetails);
                        for (WorksOnDetails newDetails : command.getWorkDetails()) {
                            entity = new WorksOnEntity(projectEntity,
                                                       employeeEntity, newDetails.getStatus(),
                                                       newDetails.getWorkload(),
                                                       LocalDate.parse(newDetails.getStartDate()),
                                                       LocalDate.parse(newDetails.getEndDate()));
                            final WorksOnEntity saved = repository.save(entity);
                            result.add(new WorksOn(saved));
                        }
                    } else {
                        for (WorksOnDetails workDetails : command.getWorkDetails()) {
                            entity = new WorksOnEntity(projectEntity,
                                                       employeeEntity, workDetails.getStatus(),
                                                       workDetails.getWorkload(),
                                                       LocalDate.parse(workDetails.getStartDate()),
                                                       LocalDate.parse(workDetails.getEndDate()));
                            final WorksOnEntity saved = repository.save(entity);
                            result.add(new WorksOn(saved));
                        }
                    }

                } else {
                    log.error("Fehler beim Speichern");
                }
            }
            return result;
        }
        return null;
    }
}
