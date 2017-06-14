package de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Matthias on 5/21/16.
 * <p>
 * Checks the ownership of a project.
 * <p>
 * A project is owned by the two mangers of the organisationUnit to which the
 * project belongs.
 */
public class ProjectInfo implements Ownership<ProjectEntity> {
    /**
     * Returns, if the object is owned by the employee with the id e.
     *
     * @param id  the id of the employee to check.
     * @param obj the object to check.
     * @return true if e is the owner of obj.
     */
    @Override
    public boolean isOwner(Integer id, ProjectEntity obj) {
        try {
            OrganisationUnitEntity orgaUnit = obj.getOrganisationUnit();

            return (id.equals(orgaUnit.getFirstManager().getId())) ||
                    (id.equals(orgaUnit.getSecondManager().getId()));
        } catch (Exception ex) { //If something goes wrong (eg. obj is null) we do not allow it.
            return false;
        }
    }

    /**
     * Compares two objects
     *
     * @param o the other object.
     * @return true if both objects are from the type of this class.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        ProjectInfo a = (ProjectInfo) o;
        return true;
    }

}
