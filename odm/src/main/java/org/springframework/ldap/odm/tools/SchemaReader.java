/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.odm.tools;

import org.springframework.ldap.odm.tools.SyntaxToJavaClass.ClassInfo;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import java.util.HashSet;
import java.util.Set;

// Processes LDAP Schema
/* package */ final class SchemaReader {
    private final DirContext schemaContext;

    private final SyntaxToJavaClass syntaxToJavaClass;

    private final Set<String> binarySet;

    public SchemaReader(DirContext schemaContext, SyntaxToJavaClass syntaxToJavaClass, Set<String> binarySet) {
        this.schemaContext = schemaContext;
        this.syntaxToJavaClass = syntaxToJavaClass;
        this.binarySet = binarySet;
    }

    // Get the object schema for the given object classes
    public ObjectSchema getObjectSchema(Set<String> objectClasses) 
        throws NamingException, ClassNotFoundException {
        
        ObjectSchema result = new ObjectSchema();
        createObjectClass(objectClasses, schemaContext, result);
        return result;
    }

    private enum SchemaAttributeType {
        SUP, MUST, MAY, UNKNOWN
    }

    private SchemaAttributeType getSchemaAttributeType(String type) {
        SchemaAttributeType result = SchemaAttributeType.UNKNOWN;

        if (type.equals("SUP")) {
            result = SchemaAttributeType.SUP;
        } else {
            if (type.equals("MUST")) {
                result = SchemaAttributeType.MUST;
            } else {
                if (type.equals("MAY")) {
                    result = SchemaAttributeType.MAY;
                }
            }
        }
        return result;
    }

    private AttributeSchema createAttributeSchema(String name, DirContext schemaContext)
        throws NamingException, ClassNotFoundException {
        
        // Get the schema definition
        Attributes attributeSchema = schemaContext.getAttributes("AttributeDefinition/" + name);

        String syntax = null;
        while(syntax == null) {
            Attribute syntaxAttribute = attributeSchema.get("SYNTAX");
            if(syntaxAttribute != null) {
                syntax = ((String)syntaxAttribute.get()).split("\\{")[0];
            } else {
                // Try to recursively retrieve syntax for super definition.
                Attribute supAttribute = attributeSchema.get("SUP");
                if(supAttribute == null) {
                    // Well, at least we tried
                    throw new IllegalArgumentException("Unable to get syntax definition for attribute " + name);
                } else {
                    attributeSchema = schemaContext.getAttributes("AttributeDefinition/" + supAttribute.get());
                }
            }
        }

        // Is it binary?
        boolean isBinary=binarySet.contains(syntax);
        
        // Use it to look up the required Java class
        ClassInfo classInfo = syntaxToJavaClass.getClassInfo(syntax);

        // Now we can set the java class
        String javaClassName = null;
        boolean isPrimitive = false;
        boolean isArray = false;
        
        if (classInfo!=null) {
            javaClassName=classInfo.getClassName();
            Class<?> javaClass=Class.forName(classInfo.getFullClassName());
            javaClassName=javaClass.getSimpleName();
            isPrimitive=javaClass.isPrimitive();
            isArray=javaClass.isArray();
        } else {
            if (isBinary) {
                javaClassName="byte[]";
                isPrimitive=false;
                isArray=true;
            } else {
                javaClassName="String";
                isPrimitive=false;
                isArray=false;
            }
        }
        
        return new AttributeSchema(name, syntax, 
                attributeSchema.get("SINGLE-VALUE") == null, 
                isPrimitive, isBinary, isArray, javaClassName);
    }

    // Recursively extract schema from the directory and process it
    private void createObjectClass(Set<String> objectClasses, DirContext schemaContext, ObjectSchema schema)
            throws NamingException, ClassNotFoundException {

        // Super classes
        Set<String> supList = new HashSet<String>();

        // For each of the given object classes
        for (String objectClass : objectClasses) {
            // Add to set of included object classes
            schema.addObjectClass(objectClass);
            
            // Grab the LDAP schema of the object class
            Attributes attributes = schemaContext.getAttributes("ClassDefinition/" + objectClass);
            NamingEnumeration<? extends Attribute> valuesEnumeration = attributes.getAll();

            // Loop through each of the attributes
            while (valuesEnumeration.hasMoreElements()) {
                Attribute currentAttribute = valuesEnumeration.nextElement();

                // Get the attribute name and lower case it (as this is all case indep)
                String currentId = currentAttribute.getID().toUpperCase();
                
                // Is this a MUST, MAY or SUP attribute
                SchemaAttributeType type = getSchemaAttributeType(currentId);

                // Loop through all the values
                NamingEnumeration<?> currentValues = currentAttribute.getAll();
                while (currentValues.hasMoreElements()) {
                    String currentValue = (String)currentValues.nextElement();
                    switch (type) {
                        case SUP:
                            // Its a super class
                            String lowerCased=currentValue.toLowerCase();
                            if (!schema.getObjectClass().contains(lowerCased)) {
                                supList.add(lowerCased);
                            }
                            break;
                        case MUST:
                            // Add must attribute
                            schema.addMust(createAttributeSchema(currentValue, schemaContext));
                            break;
                        case MAY:
                            // Add may attribute
                            schema.addMay(createAttributeSchema(currentValue, schemaContext));
                            break;
                        default:
                            // Nothing to do
                    }
                }
            }

            // Recurse for super classes
            createObjectClass(supList, schemaContext, schema);
        }
    }
}
