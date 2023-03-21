package org.springframework;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import java.util.SortedSet;

/**
 * Common data access methods for entries in an LDAP tree.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public interface LdapDataEntry {

	/**
	 * Get the value of a String attribute. If more than one attribute value exists for
	 * the specified attribute, only the first one will be returned. If an attribute has
	 * no value, <code>null</code> will be returned.
	 * @param name name of the attribute.
	 * @return the value of the attribute if it exists, or <code>null</code> if the
	 * attribute doesn't exist or if it exists but with no value.
	 * @throws ClassCastException if the value of the entry is not a String.
	 */
	String getStringAttribute(String name);

	/**
	 * Get the value of an Object attribute. If more than one attribute value exists for
	 * the specified attribute, only the first one will be returned. If an attribute has
	 * no value, <code>null</code> will be returned.
	 * @param name name of the attribute.
	 * @return the attribute value as an object if it exists, or <code>null</code> if the
	 * attribute doesn't exist or if it exists but with no value.
	 */
	Object getObjectAttribute(String name);

	/**
	 * Check if an Object attribute exists, regardless of whether it has a value or not.
	 * @param name name of the attribute
	 * @return <code>true</code> if the attribute exists, <code>false</code> otherwise
	 */
	boolean attributeExists(String name);

	/**
	 * Set the with the name <code>name</code> to the <code>value</code>. If the value is
	 * a {@link Name} instance, equality for Distinguished Names will be used for
	 * calculating attribute modifications.
	 * @param name name of the attribute.
	 * @param value value to set the attribute to.
	 * @throws IllegalArgumentException if the value is a {@link Name} instance and one or
	 * several of the currently present attribute values is <strong>not</strong>
	 * {@link Name} instances or Strings representing valid Distinguished Names.
	 */
	void setAttributeValue(String name, Object value);

	/**
	 * Sets a multivalue attribute, disregarding the order of the values.
	 *
	 * If value is null or value.length == 0 then the attribute will be removed.
	 *
	 * If update mode, changes will be made only if the array has more or less objects or
	 * if one or more object has changed. Reordering the objects will not cause an update.
	 *
	 * If the values are {@link Name} instances, equality for Distinguished Names will be
	 * used for calculating attribute modifications.
	 * @param name The id of the attribute.
	 * @param values Attribute values.
	 * @throws IllegalArgumentException if value is a {@link Name} instance and one or
	 * several of the currently present attribute values is <strong>not</strong>
	 * {@link Name} instances or Strings representing valid Distinguished Names.
	 */
	void setAttributeValues(String name, Object[] values);

	/**
	 * Sets a multivalue attribute.
	 *
	 * If value is null or value.length == 0 then the attribute will be removed.
	 *
	 * If update mode, changes will be made if the array has more or less objects or if
	 * one or more string has changed.
	 *
	 * Reordering the objects will only cause an update if orderMatters is set to true.
	 *
	 * If the values are {@link Name} instances, equality for Distinguished Names will be
	 * used for calculating attribute modifications.
	 * @param name The id of the attribute.
	 * @param values Attribute values.
	 * @param orderMatters If <code>true</code>, it will be changed even if data was just
	 * reordered.
	 * @throws IllegalArgumentException if value is a {@link Name} instance and one or
	 * several of the currently present attribute values is <strong>not</strong>
	 * {@link Name} instances or Strings representing valid Distinguished Names.
	 */
	void setAttributeValues(String name, Object[] values, boolean orderMatters);

	/**
	 * Add a value to the Attribute with the specified name. If the Attribute doesn't
	 * exist it will be created. This method makes sure that the there will be no
	 * duplicates of an added value - it the value exists it will not be added again.
	 *
	 * If the value is a {@link Name} instance, equality for Distinguished Names will be
	 * used for calculating attribute modifications.
	 * @param name the name of the Attribute to which the specified value should be added.
	 * @param value the Attribute value to add.
	 * @throws IllegalArgumentException if value is a {@link Name} instance and one or
	 * several of the currently present attribute values is <strong>not</strong>
	 * {@link Name} instances or Strings representing valid Distinguished Names.
	 */
	void addAttributeValue(String name, Object value);

	/**
	 * Add a value to the Attribute with the specified name. If the Attribute doesn't
	 * exist it will be created. The <code>addIfDuplicateExists</code> parameter controls
	 * the handling of duplicates. It <code>false</code>, this method makes sure that the
	 * there will be no duplicates of an added value - it the value exists it will not be
	 * added again.
	 *
	 * If the value is a {@link Name} instance, equality for Distinguished Names will be
	 * used for calculating attribute modifications.
	 * @param name the name of the Attribute to which the specified value should be added.
	 * @param value the Attribute value to add.
	 * @param addIfDuplicateExists <code>true</code> will add the value regardless of
	 * whether there is an identical value already, allowing for duplicate attribute
	 * values; <code>false</code> will not add the value if it already exists.
	 * @throws IllegalArgumentException if value is a {@link Name} instance and one or
	 * several of the currently present attribute values is <strong>not</strong>
	 * {@link Name} instances or Strings representing valid Distinguished Names.
	 */
	void addAttributeValue(String name, Object value, boolean addIfDuplicateExists);

	/**
	 * Remove a value from the Attribute with the specified name. If the Attribute doesn't
	 * exist, do nothing.
	 *
	 * If the value is a {@link Name} instance, equality for Distinguished Names will be
	 * used for calculating attribute modifications.
	 * @param name the name of the Attribute from which the specified value should be
	 * removed.
	 * @param value the value to remove.
	 * @throws IllegalArgumentException if value is a {@link Name} instance and one or
	 * several of the currently present attribute values is <strong>not</strong>
	 * {@link Name} instances or Strings representing valid Distinguished Names.
	 */
	void removeAttributeValue(String name, Object value);

	/**
	 * Get all values of a String attribute.
	 * @param name name of the attribute.
	 * @return a (possibly empty) array containing all registered values of the attribute
	 * as Strings if the attribute is defined or <code>null</code> otherwise.
	 * @throws IllegalArgumentException if any of the attribute values is not a String.
	 */
	String[] getStringAttributes(String name);

	/**
	 * Get all values of an Object attribute.
	 * @param name name of the attribute.
	 * @return a (possibly empty) array containing all registered values of the attribute
	 * if the attribute is defined or <code>null</code> otherwise.
	 * @since 1.3
	 */
	Object[] getObjectAttributes(String name);

	/**
	 * Get all String values of the attribute as a <code>SortedSet</code>.
	 * @param name name of the attribute.
	 * @return a <code>SortedSet</code> containing all values of the attribute, or
	 * <code>null</code> if the attribute does not exist.
	 * @throws IllegalArgumentException if one of the found attribute values cannot be
	 * cast to a String.
	 */
	SortedSet<String> getAttributeSortedStringSet(String name);

	/**
	 * Returns the DN relative to the base path. <b>NB</b>: as of version 2.0 the returned
	 * name will be an LdapName instance.
	 * @return The distinguished name of the current context.
	 *
	 * @see org.springframework.ldap.core.DirContextAdapter#getNameInNamespace()
	 */
	Name getDn();

	/**
	 * Get all the Attributes.
	 * @return all the Attributes.
	 * @since 1.3
	 */
	Attributes getAttributes();

}
