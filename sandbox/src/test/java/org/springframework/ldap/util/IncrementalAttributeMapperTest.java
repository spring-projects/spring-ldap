package org.springframework.ldap.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.Attribute;

/**
 * IncrementalAttributeMapper Tester.
 *
 * @author Marius Scurtescu
 */
public class IncrementalAttributeMapperTest extends TestCase
{
    private IncrementalAttributeMapper _incrementalAttributeMapper;
    private ListAttributeValueProcessor _valueProcessor;

    public IncrementalAttributeMapperTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        _valueProcessor = new ListAttributeValueProcessor();
        _incrementalAttributeMapper = new IncrementalAttributeMapper("member", _valueProcessor);
    }

    public void tearDown() throws Exception
    {
        _incrementalAttributeMapper = null;
        _valueProcessor = null;

        super.tearDown();
    }

    public void testGetAttributesArray() throws Exception
    {
        String[] attributes = _incrementalAttributeMapper.getAttributesArray();

        assertEquals(1, attributes.length);
        assertEquals("member", attributes[0]);

        _incrementalAttributeMapper = new IncrementalAttributeMapper("member", _valueProcessor, 10);

        attributes = _incrementalAttributeMapper.getAttributesArray();

        assertEquals(1, attributes.length);
        assertEquals("member;Range=0-10", attributes[0]);
    }

    public void testLoopEmpty() throws Exception
    {
        assertTrue(_incrementalAttributeMapper.hasMore());

        Attributes attributes = new BasicAttributes();

        _incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(_incrementalAttributeMapper.hasMore());
        assertEquals(0, _valueProcessor.getValues().size());
    }

    public void testLoop() throws Exception
    {
        Attributes attributes = createAttributes(new RangeOption(0, 10), true);

        _incrementalAttributeMapper.mapFromAttributes(attributes);

        assertTrue(_incrementalAttributeMapper.hasMore());
        assertEquals(11, _valueProcessor.getValues().size());

        attributes = createAttributes(new RangeOption(11), 5, false);

        _incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(_incrementalAttributeMapper.hasMore());
        assertEquals(16, _valueProcessor.getValues().size());
    }

    public void test1LoopWithPageSizeExact() throws Exception
    {
        _incrementalAttributeMapper = new IncrementalAttributeMapper("member", _valueProcessor, 10);

        Attributes attributes = createAttributes(new RangeOption(0, 10), true);

        _incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(_incrementalAttributeMapper.hasMore());
        assertEquals(11, _valueProcessor.getValues().size());
    }

    public void test2LoopsWithPageSizeExact() throws Exception
    {
        _incrementalAttributeMapper = new IncrementalAttributeMapper("member", _valueProcessor, 20);

        Attributes attributes = createAttributes(new RangeOption(0, 10), true);

        _incrementalAttributeMapper.mapFromAttributes(attributes);

        assertTrue(_incrementalAttributeMapper.hasMore());
        assertEquals(11, _valueProcessor.getValues().size());

        attributes = createAttributes(new RangeOption(11, 30), false);

        _incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(_incrementalAttributeMapper.hasMore());
        assertEquals(31, _valueProcessor.getValues().size());
    }

    public void test2LoopsWithPageSize() throws Exception
    {
        _incrementalAttributeMapper = new IncrementalAttributeMapper("member", _valueProcessor, 20);

        Attributes attributes = createAttributes(new RangeOption(0, 10), true);

        _incrementalAttributeMapper.mapFromAttributes(attributes);

        assertTrue(_incrementalAttributeMapper.hasMore());
        assertEquals(11, _valueProcessor.getValues().size());

        attributes = createAttributes(new RangeOption(11), 5, false);

        _incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(_incrementalAttributeMapper.hasMore());
        assertEquals(16, _valueProcessor.getValues().size());
    }

    private Attributes createAttributes(RangeOption range, boolean emptyPlain)
    {
        return createAttributes(range, range.getTerminal() - range.getInitial() + 1, emptyPlain);
    }

    private Attributes createAttributes(RangeOption range, int valueCnt, boolean emptyPlain)
    {
        Attributes attributes = new BasicAttributes();

        if (emptyPlain)
            attributes.put(new BasicAttribute("member"));

        Attribute attribute = new BasicAttribute("member;" + range.toString());
        for (int i = 0; i < valueCnt; i++)
            attribute.add("value" + (range.getInitial() + i - 1));
        attributes.put(attribute);

        return attributes;

    }

    public static Test suite()
    {
        return new TestSuite(IncrementalAttributeMapperTest.class);
    }
}
