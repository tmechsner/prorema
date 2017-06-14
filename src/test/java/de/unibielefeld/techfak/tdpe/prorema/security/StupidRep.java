package de.unibielefeld.techfak.tdpe.prorema.security;

import org.springframework.data.repository.CrudRepository;

public class StupidRep implements CrudRepository<StupidEntity, Integer> {

    @Override
    public <S extends StupidEntity> S save(S s) {
        return null;
    }

    @Override
    public <S extends StupidEntity> Iterable<S> save(Iterable<S> iterable) {
        return null;
    }

    @Override
    public StupidEntity findOne(Integer integer) {
        return new StupidEntity();
    }

    @Override
    public boolean exists(Integer integer) {
        return false;
    }

    @Override
    public Iterable<StupidEntity> findAll() {
        return null;
    }

    @Override
    public Iterable<StupidEntity> findAll(Iterable<Integer> iterable) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(Integer integer) {

    }

    @Override
    public void delete(StupidEntity stupidEntity) {

    }

    @Override
    public void delete(Iterable<? extends StupidEntity> iterable) {

    }

    @Override
    public void deleteAll() {

    }
}

