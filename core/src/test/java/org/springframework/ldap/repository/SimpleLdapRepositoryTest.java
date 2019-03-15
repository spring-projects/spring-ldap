/*
 * Copyright 2005-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Persistable;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.repository.support.SimpleLdapRepository;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.Arrays;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Mattias Hellborg Arthursson
 */
public class SimpleLdapRepositoryTest {

    private LdapOperations ldapOperationsMock;
    private ObjectDirectoryMapper odmMock;
    private SimpleLdapRepository<Object> tested;

    @Before
    public void prepareTestedInstance() {
        ldapOperationsMock = mock(LdapOperations.class);
        odmMock = mock(ObjectDirectoryMapper.class);
        tested = new SimpleLdapRepository<Object>(ldapOperationsMock, odmMock, Object.class);
    }

    @Test
    public void testCount() {
        Filter filterMock = mock(Filter.class);
        when(odmMock.filterFor(Object.class, null)).thenReturn(filterMock);
        ArgumentCaptor<LdapQuery> ldapQuery = ArgumentCaptor.forClass(LdapQuery.class);
        doNothing().when(ldapOperationsMock).search(ldapQuery.capture(), any(CountNameClassPairCallbackHandler.class));

        long count = tested.count();

        assertThat(count).isEqualTo(0);
        LdapQuery query = ldapQuery.getValue();
        assertThat(query.filter()).isEqualTo(filterMock);
        assertThat(query.attributes()).isEqualTo(new String[]{"objectclass"});
    }

    @Test
    public void testSaveNonPersistableWithIdSet() {
        Object expectedEntity = new Object();

        when(odmMock.getId(expectedEntity)).thenReturn(LdapUtils.emptyLdapName());
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(null);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).update(expectedEntity);
    }

    @Test
    public void testSaveNonPersistableWithIdChanged() {
        Object expectedEntity = new Object();
        LdapName expectedName = LdapUtils.newLdapName("ou=newlocation");

        when(odmMock.getId(expectedEntity)).thenReturn(LdapUtils.emptyLdapName());
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(expectedName);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).update(expectedEntity);
    }

    @Test
    public void testSaveNonPersistableWithNoIdCalculatedId() {
        Object expectedEntity = new Object();
        LdapName expectedName = LdapUtils.emptyLdapName();

        when(odmMock.getId(expectedEntity)).thenReturn(null);
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(expectedName);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).create(expectedEntity);
    }

    @Test
    public void testSavePersistableNewWithDeclaredId() {
        Persistable expectedEntity = mock(Persistable.class);

        when(expectedEntity.isNew()).thenReturn(true);
        when(odmMock.getId(expectedEntity)).thenReturn(LdapUtils.emptyLdapName());
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(null);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).create(expectedEntity);
    }

    @Test
    public void testSavePersistableNewWithCalculatedId() {
        Persistable expectedEntity = mock(Persistable.class);
        LdapName expectedName = LdapUtils.emptyLdapName();

        when(expectedEntity.isNew()).thenReturn(true);
        when(odmMock.getId(expectedEntity)).thenReturn(null);
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(expectedName);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).create(expectedEntity);
    }

    @Test
    public void testSavePersistableNotNew() {
        Persistable expectedEntity = mock(Persistable.class);

        when(expectedEntity.isNew()).thenReturn(false);
        when(odmMock.getId(expectedEntity)).thenReturn(LdapUtils.emptyLdapName());
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(null);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).update(expectedEntity);
    }

    @Test
    public void testFindOneWithName() {
        LdapName expectedName = LdapUtils.emptyLdapName();
        Object expectedResult = new Object();

        when(ldapOperationsMock.findByDn(expectedName, Object.class)).thenReturn(expectedResult);

        Object actualResult = tested.findOne(expectedName);

        assertThat(actualResult).isSameAs(expectedResult);
    }

    @Test
    public void verifyThatNameNotFoundInFindOneWithNameReturnsNull() {
        LdapName expectedName = LdapUtils.emptyLdapName();

        when(ldapOperationsMock.findByDn(expectedName, Object.class)).thenThrow(new NameNotFoundException(""));

        Object actualResult = tested.findOne(expectedName);

        assertThat(actualResult).isNull();
    }

    @Test
    public void testFindAll() {
        Name expectedName1 = LdapUtils.newLdapName("ou=aa");
        Name expectedName2 = LdapUtils.newLdapName("ou=bb");

        Object expectedResult1 = new Object();
        Object expectedResult2 = new Object();

        when(ldapOperationsMock.findByDn(expectedName1, Object.class)).thenReturn(expectedResult1);
        when(ldapOperationsMock.findByDn(expectedName2, Object.class)).thenReturn(expectedResult2);

        Iterable<Object> actualResult = tested.findAll(Arrays.asList(expectedName1, expectedName2));

        Iterator<Object> iterator = actualResult.iterator();
        assertThat(iterator.next()).isSameAs(expectedResult1);
        assertThat(iterator.next()).isSameAs(expectedResult2);

        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void testFindAllWhereOneEntryIsNotFound() {
        Name expectedName1 = LdapUtils.newLdapName("ou=aa");
        Name expectedName2 = LdapUtils.newLdapName("ou=bb");

        Object expectedResult2 = new Object();

        when(ldapOperationsMock.findByDn(expectedName1, Object.class)).thenReturn(null);
        when(ldapOperationsMock.findByDn(expectedName2, Object.class)).thenReturn(expectedResult2);

        Iterable<Object> actualResult = tested.findAll(Arrays.asList(expectedName1, expectedName2));

        Iterator<Object> iterator = actualResult.iterator();
        assertThat(iterator.next()).isSameAs(expectedResult2);

        assertThat(iterator.hasNext()).isFalse();
    }

}
