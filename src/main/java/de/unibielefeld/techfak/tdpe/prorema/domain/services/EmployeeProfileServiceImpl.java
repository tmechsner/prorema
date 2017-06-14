package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.domain.EmployeeProfile;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeProfileRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex Schneider.
 */
@Service
@Primary
public class EmployeeProfileServiceImpl
        extends AbstactServiceImpl<EmployeeProfile, EmployeeProfileRepository, EmployeeProfileEntity>
        implements EmployeeProfileService {

    private final EmployeeRepository employeeRepository;

    /**
     * Constructor.
     *
     * @param repository         autowired
     * @param employeeRepository autowired
     */
    @Autowired
    public EmployeeProfileServiceImpl(EmployeeProfileRepository repository,
                                      EmployeeRepository employeeRepository) {
        super(repository);
        this.employeeRepository = employeeRepository;
        accessDecider = AccessDeciderPool.employeeProfile;
    }

    @Override
    protected EmployeeProfile init(EmployeeProfileEntity entity) {
        return new EmployeeProfile(entity);
    }

    @Override
    public EmployeeProfile createProfile(@NonNull EmployeeEntity employee, @NonNull String url) {
        EmployeeProfileEntity employeeProfileEntity = new EmployeeProfileEntity(url, employee);
        accessDecider.isAllowedThrow(Action.CREATE, employeeProfileEntity);
        EmployeeProfileEntity saved = repository.save(employeeProfileEntity);
        return new EmployeeProfile(saved);
    }

    @Override
    public List<EmployeeProfile> findProfileByEmployee(int employeeId) {
        ArrayList<EmployeeProfile> list = new ArrayList<>();
        repository.findById(employeeId).forEach(profile -> list.add(new EmployeeProfile(profile)));
        return list;
    }
}
