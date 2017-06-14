package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.services.AbstactServiceImpl;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Matthias on 5/27/16.
 */
public class StupidService extends AbstactServiceImpl <Stupid, StupidRep, StupidEntity> {
    public StupidService(StupidRep repository) {
        super(repository);
    }

    public void setAccessDecider(AccessDecider accessDecider) {
       this.accessDecider = accessDecider;
    }

    @Override
    protected Stupid init(StupidEntity entity) {
        return new Stupid();
    }

}
