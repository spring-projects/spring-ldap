//CHECKSTYLE:OFF FileLength

/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.typeconversion;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Method;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.entity.SomeEntityWithGenericCollection;
import org.springframework.core.MethodParameter;

public class LdapTypeConverterTest extends TestCase
{
    private static final Log LOGGER = LogFactory.getLog(LdapTypeConverterTest.class);

    private LdapTypeConverter typeConverter;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        typeConverter = new LdapTypeConverter();
    }

    public void testPassthroughOfByteArray()
    {
        byte[] objectToTranslate = "fred".getBytes();

        try
        {
            byte[] translated = (byte[]) typeConverter.convertIfNecessary(
                    objectToTranslate, byte[].class);
            Assert.assertTrue("byte[] attributes should pass through unchanged.",
                    Arrays.equals(objectToTranslate, translated));
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception: byte[] attributes should pass through unchanged.");
        }
    }

    public void testConvertStringToBoolean()
    {
        String objectToTranslate = "true";
        try
        {
            Object translated = typeConverter.convertIfNecessary(
                    objectToTranslate, Boolean.class);
            Assert.assertEquals(true, translated);

        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testConvertBooleanToString()
    {
        boolean objectToTranslate = false;
        try
        {
            Object translated = typeConverter.getAsText(objectToTranslate);
            Assert.assertEquals(translated, "false");

        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testPassthroughOfString()
    {
        String objectToTranslate = "onetwothree";
        try
        {
            Object translated = typeConverter.convertIfNecessary(
                    objectToTranslate, String.class);
            Assert.assertEquals("onetwothree", translated);
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testPassthroughOfStringArray()
    {
        String object1 = "onetwothree";
        String object2 = "fourfivesix";
        String object3 = "seveneightnine";
        Object objectToTranslate = new String[]{object1, object2, object3};

        try
        {
            Object[] translated = (Object[]) typeConverter.convertIfNecessary(
                    objectToTranslate, String[].class);
            Assert.assertEquals("fourfivesix", translated[1]);
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testConvertStringArrayToListOfStrings()
    {
        String object1 = "onetwothree";
        String object2 = "fourfivesix";
        String object3 = "seveneightnine";
        Object objectToTranslate = new String[]{object1, object2, object3};

        try
        {
            List<String> translated = (List<String>) typeConverter.convertIfNecessary(
                    objectToTranslate, List.class);
            Assert.assertEquals("fourfivesix", translated.get(1));
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }


    public void testConvertStringArrayToSetOfStrings()
    {
        String object1 = "onetwothree";
        String object2 = "fourfivesix";
        String object3 = "seveneightnine";
        Object objectToTranslate = new String[]{object1, object2, object3};

        try
        {
            Set<String> translated = (Set<String>) typeConverter.convertIfNecessary(
                    objectToTranslate, Set.class);
            Assert.assertTrue(translated.contains("fourfivesix"));
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    } 




    public void testConvertStringToDate() throws ParseException
    {
        String object1 = "19700101100000.000+1000";

        try
        {
            Object translated = typeConverter.convertIfNecessary(object1,
                    Date.class);
            Assert.assertEquals(translated, new Date(0L));

        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testConvertDateToString()
    {
        Date source = new Date(1184283822285L); //Friday July 13, 2007 9:43:42 AM GMT+1000
        LOGGER.debug(typeConverter.getAsText(source));

        Assert.assertTrue(typeConverter.getAsText(source)
                .matches("20070713\\d\\d\\d\\d42.285\\+\\d\\d\\d\\d"));
    }

    public void testConvertStringArrayToDateArray() throws ParseException
    {
        String object1 = "20071105093655.0+1000";
        String object2 = "19700101100000.0+1000";
        Object objectToTranslate = new String[]{object1, object2};

        try
        {
            Object[] translated = (Object[]) typeConverter.convertIfNecessary(
                    objectToTranslate, Date[].class);
            Assert.assertEquals(translated[1], new Date(0L));
        }
        catch (TypeMismatchException e)
        {
            e.printStackTrace();
            Assert.fail("Unexpected exception during translation");

        }
    }

    public void testConvertGeneralizedTimeArrayToListOfDates() throws ParseException, NoSuchMethodException
    {
        String object1 = "20071105093655.0+1000";
        String object2 = "19700101100000.0+1000";
        Object objectToTranslate = new String[]{object1, object2};

        try
        {
            Method setter = SomeEntityWithGenericCollection.class.getMethod("setActiveDates", List.class);
            MethodParameter methodParameter = MethodParameter.forMethodOrConstructor(setter, 0);
            List<Date> translated = (List<Date>) typeConverter.convertIfNecessary(
                    objectToTranslate, List.class, methodParameter);
            LOGGER.debug(translated);
            Assert.assertTrue(translated.contains(new Date(0L)));
        }
        catch (TypeMismatchException e)
        {
            e.printStackTrace();
            Assert.fail("Unexpected exception during translation");

        }
    }


    public void testConvertDateArrayToStringArray()
    {
        Date date1 = new Date(1184283822285L); //Friday July 13, 2007 9:43:42 AM + GMT+1000
        Date date2 = new Date(0); //epoch + GMT + 1000

        Date[] dates = new Date[]{date1, date2};
        String[] converted = typeConverter.getAllAsText(dates);
        Assert.assertTrue(converted[0].matches("20070713\\d\\d\\d\\d42.285\\+\\d\\d\\d\\d"));
        Assert.assertTrue(converted[1].matches("19700101\\d\\d\\d\\d00.000\\+\\d\\d\\d\\d"));
    }

    public void testConvertDateCollectionToStringArray()
    {
        Date date1 = new Date(1184283822285L); //Friday July 13, 2007 9:43:42 AM + GMT+1000
        Date date2 = new Date(0); //epoch + GMT + 1000

        Set<Date> dates = new HashSet<Date>();
        dates.add(date1);
        dates.add(date2);
        String[] converted = typeConverter.getAllAsText(dates);
        Assert.assertTrue(converted[0].matches("20070713\\d\\d\\d\\d42.285\\+\\d\\d\\d\\d"));
        Assert.assertTrue(converted[1].matches("19700101\\d\\d\\d\\d00.000\\+\\d\\d\\d\\d"));
    }


    public void testConvertStringToLdapName()
    {
        String object1 = "uid=amAdmin, ou = people, dc = myretsu,dc=com";
        try
        {
            Object translated = typeConverter.convertIfNecessary(object1,
                    LdapName.class);
            Assert.assertEquals(translated, new LdapName(object1));
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
        catch (InvalidNameException e)
        {
            Assert.fail("Problem with test: Ldap name can't be parsed.");
        }
    }

    public void testConvertStringArrayToLdapNameArray()
    {
        String object1 = "uid=amAdmin, ou = people, dc = myretsu,dc=com";
        String object2 = "uid=fred, ou = people, dc = myretsu,dc=com";
        Object objectToTranslate = new String[]{object1, object2};

        try
        {
            Object[] translated = (Object[]) typeConverter.convertIfNecessary(
                    objectToTranslate, LdapName[].class);
            Assert.assertEquals(new LdapName(object2), translated[1]);
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
        catch (InvalidNameException e)
        {
            Assert.fail("Problem with test: Ldap name can't be parsed.");
        }
    }

    public void testConvertStringToDistinguishedName()
    {
        String object1 = "uid=amAdmin, ou = people, dc = myretsu,dc=com";
        try
        {
            Object translated = typeConverter.convertIfNecessary(object1,
                    DistinguishedName.class);
            Assert.assertEquals(new DistinguishedName(object1), translated);
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testConvertStringArrayToDistinguishedNameArray()
    {
        String object1 = "uid=amAdmin, ou = people, dc = myretsu,dc=com";
        String object2 = "uid=fred, ou = people, dc = myretsu,dc=com";
        Object objectToTranslate = new String[]{object1, object2};

        try
        {
            Object[] translated = (Object[]) typeConverter.convertIfNecessary(
                    objectToTranslate, DistinguishedName[].class);
            Assert.assertEquals(new DistinguishedName(object2), translated[1]);
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testConvertStringToLong()
    {
        String object1 = "9887342";
        try
        {
            Object translated = typeConverter.convertIfNecessary(object1,
                    Long.class);
            Assert.assertEquals(9887342L, translated);
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testConvertStringArrayToLongArray()
    {
        String object1 = "878787";
        String object2 = "23948787";
        Object objectToTranslate = new String[]{object1, object2};

        try
        {
            Object[] translated = (Object[]) typeConverter.convertIfNecessary(
                    objectToTranslate, Long[].class);
            Assert.assertEquals(23948787L, translated[1]);

        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testConvertStringToInteger()
    {
        String object1 = "9887342";

        try
        {
            Object translated = typeConverter.convertIfNecessary(object1,
                    Integer.class);
            Assert.assertEquals(9887342, translated);
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testConvertStringArrayToIntegerArray()
    {
        String object1 = "878787";
        String object2 = "23948787";
        Object objectToTranslate = new String[]{object1, object2};

        try
        {
            Object[] translated = (Object[]) typeConverter.convertIfNecessary(
                    objectToTranslate, Integer[].class);
            Assert.assertEquals(23948787, translated[1]);
        }
        catch (TypeMismatchException e)
        {
            Assert.fail("Unexpected exception during translation");
        }
    }

    public void testThrowsTypeMismatchExceptionWhenTypeTranslationFails()
    {
        String object1 = "asdlkjkalkjl";

        Object[] translated;
        try
        {
            translated = (Object[]) typeConverter.convertIfNecessary(object1,
                    LdapName.class);
            Assert.fail("Should've thrown exception");
        }
        catch (TypeMismatchException e)
        {
            // Pass
        }
    }

    public void testGetAsTextReturnsStringsUnchanged()
    {
        String string = "onetwothree123";
        Assert.assertEquals(typeConverter.getAsText(string), "onetwothree123");
    }

    public void testGetAsTextReturnsNullWhenNoPropertyEditorRegistered()
    {
        Assert.assertNull(typeConverter.getAsText(new Foo("fooString")));
    }

    //Something there'll definitely be no property editor registered for
    private class Foo
    {
        private String fooString;

        public Foo(String fooString)
        {
            this.fooString = fooString;
        }
    }

}
//CHECKSTYLE:ON FileLength
