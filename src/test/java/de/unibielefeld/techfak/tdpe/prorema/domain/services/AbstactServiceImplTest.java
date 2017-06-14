package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.security.AccessDecider;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.repository.CrudRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * Created by x4fyr on 7/1/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstactServiceImplTest {

    private static Integer MAX_LIST_SIZE = 1000;

    private Random random = new Random();

    @Mock private DomainStub domain;
    @Mock private EntityStub entity;
    @Mock private CrudRepository<EntityStub, Integer> repo;
    @Mock private AccessDecider<EntityStub> decider;

    AbstractServiceImplStub service;

    @Before
    public void setUp() throws Exception {
        service = new AbstractServiceImplStub();
    }

    @Test
    public void findOne() throws Exception {
        // given
        Integer id = random.nextInt();
        given(repo.findOne(id)).willReturn(entity);
        given(decider.isAllowed(Action.VIEW, entity)).willReturn(true);
        // when
        DomainStub result = service.findOne(id);
        // then
        assertThat(result).as("entity exist and access is granted").isEqualTo(domain);

        // given
        given(repo.findOne(id)).willReturn(null);
        // when
        result = service.findOne(id);
        // then
        assertThat(result).as("entity does not exist").isNull();
    }

    @Test(expected = PermissionDeniedException.class)
    public void findOneAccessDenied() {
        // given
        Integer id = random.nextInt();
        given(decider.isAllowed(Action.VIEW, entity)).willReturn(false);
        given(repo.findOne(id)).willReturn(entity);

        // when
        DomainStub result = service.findOne(id);
    }

    @Test
    public void findAll() throws Exception {
        // given
        List<Integer> ids = random.ints().boxed().limit(MAX_LIST_SIZE).collect(Collectors.toList());
        List<EntityStub> entities = new LinkedList<>();
        ids.forEach(integer -> entities.add(new EntityStub(integer)));
        given(repo.findAll(ids)).willReturn(entities);
        given(decider.isAllowed(eq(Action.VIEW), any(EntityStub.class))).willReturn(true);
        // when
        List<DomainStub> result = service.findAll(ids);
        // then
        assertThat(result).as("entities exist and access is granted")
                          .extracting("id")
                          .containsExactlyElementsOf(ids);

        // given
        given(repo.findAll(ids)).willReturn(new LinkedList<>());
        // when
        result = service.findAll(ids);
        // then
        assertThat(result).as("no entites are found").isNotNull().isEmpty();
        verify(repo, times(2)).findAll(anyCollectionOf(Integer.class));
    }

    //@Ignore(expected = PermissionDeniedException.class)
    public void findAllPermissionDenied() {
        // given
        List<Integer> ids = random.ints().boxed().limit(MAX_LIST_SIZE).collect(Collectors.toList());
        List<EntityStub> entities = new LinkedList<>();
        ids.forEach(integer -> entities.add(new EntityStub(integer)));
        given(repo.findAll(ids)).willReturn(entities);
        given(decider.isAllowed(eq(Action.VIEW), any(EntityStub.class))).willReturn(false);
        // when
        List<DomainStub> result = service.findAll(ids);
    }

    @Test
    public void getAll() throws Exception {
        // given
        List<Integer> ids = random.ints().boxed().limit(MAX_LIST_SIZE).collect(Collectors.toList());
        List<EntityStub> entities = new LinkedList<>();
        ids.forEach(integer -> entities.add(new EntityStub(integer)));
        given(repo.findAll()).willReturn(entities);
        given(decider.isAllowed(eq(Action.VIEW), any(EntityStub.class))).willReturn(true);
        // when
        List<DomainStub> result = service.getAll();
        // then
        assertThat(result).as("entities exist and access is granted")
                          .extracting("id")
                          .containsExactlyElementsOf(ids);


        // given
        given(repo.findAll()).willReturn(new LinkedList<>());
        // when
        result = service.getAll();
        // then
        assertThat(result).as("no entites are found").isNotNull().isEmpty();
        verify(repo, times(2)).findAll();

    }

    //@Test(expected = PermissionDeniedException.class)
    public void getAllPermissionDenied() {
        // given
        List<Integer> ids = random.ints().boxed().limit(MAX_LIST_SIZE).collect(Collectors.toList());
        List<EntityStub> entities = new LinkedList<>();
        ids.forEach(integer -> entities.add(new EntityStub(integer)));
        given(repo.findAll()).willReturn(entities);
        given(repo.findAll()).willReturn(entities);
        willThrow(PermissionDeniedException.class).given(decider).isAllowedThrow(eq(Action.VIEW), any(EntityStub.class));
        // when
        List<DomainStub> result = service.getAll();
    }

    @Test
    public void getFiltered() throws Exception {
        // given
        List<Integer> ids = random.ints().boxed().limit(MAX_LIST_SIZE).collect(Collectors.toList());
        List<EntityStub> entities = new LinkedList<>();
        ids.forEach(integer -> entities.add(new EntityStub(integer)));
        given(repo.findAll()).willReturn(entities);
        given(decider.isAllowed(eq(Action.VIEW), any(EntityStub.class))).willReturn(true);
        Predicate<DomainStub> filter = domainStub -> domainStub.getId() > 10;
        // when
        List<DomainStub> result = service.getFiltered(filter);
        // then
        assertThat(result).as("entities exist and access is granted")
                          .extracting("id")
                          .containsExactlyElementsOf(ids.stream().filter(integer -> integer > 10).collect(Collectors.toList()));
        verify(repo).findAll();
    }

    @Test
    public void delete() throws Exception {
        // given
        Integer id = random.nextInt();
        given(repo.findOne(id)).willReturn(entity);
        given(decider.isAllowed(Action.DELETE, entity)).willReturn(true);
        // when
        boolean result = service.delete(id);
        //then
        assertThat(result).as("entity exists and acces granted").isTrue();
        verify(repo).delete(entity);

        // given
        given(decider.isAllowed(Action.DELETE, entity)).willReturn(false);
        // when
        result = service.delete(id);
        // then
        assertThat(result).as("entity exists, but acess is denied").isFalse();

        //given
        given(repo.findOne(id)).willReturn(null);
        // when
        result = service.delete(id);
        // then
        assertThat(result).as("entity does not exists").isFalse();

    }

    private class AbstractServiceImplStub extends AbstactServiceImpl<DomainStub, CrudRepository<EntityStub, Integer>, EntityStub> {

        AbstractServiceImplStub() {
            super(repo);
            accessDecider = decider;
        }

        @Override protected DomainStub init(EntityStub entityStub) {
            if (entityStub.equals(entity)) {
                return domain;
            }
            return new DomainStub(entityStub.getId());
        }
    }

    @Getter
    @RequiredArgsConstructor
    private class DomainStub {
        @NonNull
        private Integer id;
    }

    @Getter
    @RequiredArgsConstructor
    private class EntityStub {
        @NonNull
        private Integer id;


    }
}