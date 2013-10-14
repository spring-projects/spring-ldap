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
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

        assertEquals(0, count);
        LdapQuery query = ldapQuery.getValue();
        assertEquals(filterMock, query.filter());
        assertArrayEquals(new String[]{"objectclass"}, query.attributes());
    }

    @Test(expected = IllegalStateException.class)
    public void testSaveNonPersistableNoIdNoCalculatedId() {
        Object expectedEntity = new Object();

        when(odmMock.getId(expectedEntity)).thenReturn(null);
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(null);

        tested.save(expectedEntity);
    }

    @Test
    public void testSaveNonPersistableWithIdSet() {
        Object expectedEntity = new Object();

        when(odmMock.getId(expectedEntity)).thenReturn(LdapUtils.emptyLdapName());
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(null);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).update(expectedEntity);
        verify(odmMock, never()).setId(any(Object.class), any(Name.class));
    }

    @Test
    public void testSaveNonPersistableWithIdChanged() {
        Object expectedEntity = new Object();
        LdapName expectedName = LdapUtils.newLdapName("ou=newlocation");

        when(odmMock.getId(expectedEntity)).thenReturn(LdapUtils.emptyLdapName());
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(expectedName);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).update(expectedEntity);
        verify(odmMock).setId(expectedEntity, expectedName);
    }

    @Test
    public void testSaveNonPersistableWithNoIdCalculatedId() {
        Object expectedEntity = new Object();
        LdapName expectedName = LdapUtils.emptyLdapName();

        when(odmMock.getId(expectedEntity)).thenReturn(null);
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(expectedName);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).create(expectedEntity);
        verify(odmMock).setId(expectedEntity, expectedName);
    }

    @Test
    public void testSavePersistableNewWithDeclaredId() {
        Persistable expectedEntity = mock(Persistable.class);

        when(expectedEntity.isNew()).thenReturn(true);
        when(odmMock.getId(expectedEntity)).thenReturn(LdapUtils.emptyLdapName());
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(null);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).create(expectedEntity);
        verify(odmMock, never()).setId(any(Object.class), any(Name.class));
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
        verify(odmMock).setId(expectedEntity, expectedName);
    }

    @Test
    public void testSavePersistableNotNew() {
        Persistable expectedEntity = mock(Persistable.class);

        when(expectedEntity.isNew()).thenReturn(false);
        when(odmMock.getId(expectedEntity)).thenReturn(LdapUtils.emptyLdapName());
        when(odmMock.getCalculatedId(expectedEntity)).thenReturn(null);

        tested.save(expectedEntity);

        verify(ldapOperationsMock).update(expectedEntity);
        verify(odmMock, never()).setId(any(Object.class), any(Name.class));
    }

    @Test
    public void testFindOneWithName() {
        LdapName expectedName = LdapUtils.emptyLdapName();
        Object expectedResult = new Object();

        when(ldapOperationsMock.findByDn(expectedName, Object.class)).thenReturn(expectedResult);

        Object actualResult = tested.findOne(expectedName);

        assertSame(expectedResult, actualResult);
    }

    @Test
    public void verifyThatNameNotFoundInFindOneWithNameReturnsNull() {
        LdapName expectedName = LdapUtils.emptyLdapName();

        when(ldapOperationsMock.findByDn(expectedName, Object.class)).thenThrow(new NameNotFoundException(""));

        Object actualResult = tested.findOne(expectedName);

        assertNull(actualResult);
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
        assertSame(expectedResult1, iterator.next());
        assertSame(expectedResult2, iterator.next());

        assertFalse(iterator.hasNext());
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
        assertSame(expectedResult2, iterator.next());

        assertFalse(iterator.hasNext());
    }

}
