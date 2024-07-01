package com.tsystems.wsdldoc;

import com.predic8.schema.*;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Part;

import java.util.*;

import static com.tsystems.wsdldoc.TypesMapper.getLongName;

/**
 * By: Alexey Matveev
 * Date: 25.08.2016
 * Time: 17:53
 */
public class TypesLocator {

    /**
     * Creates map of destination DTO types to be handled by FTL.
     */
    public static Map<String, TypeData> createMapOfTypes(Definitions defs,
                                                         Map<String, TypeDefinition> mapOfOriginalTypes,
                                                         Map<String, Element> mapOfElements) {
        Map<String, TypeData> result = new TreeMap<>();
        // get all messages
        // for each get parts
        // part -> element -> schema
        // if schema != null -> get complex types
        // complex type.model is the place
        // complex type name is unique
        // simple type name
        // schema has imports -> other import schemas
        Set<String> alreadyCheckedSchemas = new HashSet<>();
        for (Message message : defs.getMessages()) {
            for (Part part : message.getParts()) {
                Element element = part.getElement();
                if (element != null) {
                    Schema schema = element.getSchema();
                    fillMapRecursively(result, schema, alreadyCheckedSchemas, mapOfOriginalTypes, mapOfElements);
                }
            }
        }
        return result;
    }

    public static void fillMapRecursively(Map<String, TypeData> result,
                                          Schema schema,
                                          Set<String> alreadyCheckedSchemas,
                                          Map<String, TypeDefinition> mapOfOriginalTypes,
                                          Map<String, Element> mapOfElements) {
        if (schema != null) {
            List<ComplexType> complexTypes = schema.getComplexTypes();
            if (complexTypes != null) {
                for (ComplexType ct : complexTypes) {
                    String longName = getLongName(ct.getSchema().getTargetNamespace(), ct.getName());
                    result.put(longName, TypesMapper.map2ComplexType(ct, mapOfOriginalTypes, mapOfElements));
                }
            }
            List<SimpleType> simpleTypes = schema.getSimpleTypes();
            if (simpleTypes != null) {
                for (SimpleType st : simpleTypes) {
                    String longName = getLongName(st.getSchema().getTargetNamespace(), st.getName());
                    result.put(longName, TypesMapper.map2SimpleType(st));
                }
            }
            List<Schema> importedSchemas = schema.getImportedSchemas();
            if (importedSchemas != null) {
                for (Schema importedSchema : importedSchemas) {
                    if (!alreadyCheckedSchemas.contains(importedSchema.getSchemaLocation())) {
                        alreadyCheckedSchemas.add(importedSchema.getSchemaLocation());
                        fillMapRecursively(result,
                                           importedSchema,
                                           alreadyCheckedSchemas,
                                           mapOfOriginalTypes,
                                           mapOfElements);
                    }
                }
            }
        }
    }

    /**
     * Map of original complex and simple typed found in schemas.
     * Map is created for lookup types.
     */
    public static Map<String, TypeDefinition> createMapOfOriginalTypes(Definitions defs) {
        Map<String, TypeDefinition> result = new TreeMap<>();
        Set<String> alreadyCheckedSchemas = new HashSet<>();
        for (Message message : defs.getMessages()) {
            System.out.printf("Message %s%n", message.getName());
            for (Part part : message.getParts()) {
                Element element = part.getElement();
                if (element != null) {
                    Schema schema = element.getSchema();
                    fillOriginalMapRecursively(result, schema, alreadyCheckedSchemas);
                }
            }
        }
        System.out.println("Got " + result.size() + " types");
        return result;
    }

    public static void fillOriginalMapRecursively(Map<String, TypeDefinition> result, Schema schema, Set<String> alreadyCheckedSchemas) {
        if (schema != null) {
            List<Schema> importedSchemas = schema.getImportedSchemas();
            if (importedSchemas != null) {
                for (Schema importedSchema : importedSchemas) {
                    String path = importedSchema.getTargetNamespace();
                    if (alreadyCheckedSchemas.add(path)) {
                        fillOriginalMapRecursively(result, importedSchema, alreadyCheckedSchemas);
                        System.out.printf("Added schema %s to list of already checked schemas%n", path);
                    }
                }
            }
            List<ComplexType> complexTypes = schema.getComplexTypes();
            if (complexTypes != null) {
                for (ComplexType ct : complexTypes) {
                    String longName = getLongName(ct.getSchema().getTargetNamespace(), ct.getName());
                    System.out.printf("Add complex type %s%n", longName);
                    result.put(longName, ct);
                }
            }
            List<SimpleType> simpleTypes = schema.getSimpleTypes();
            if (simpleTypes != null) {
                for (SimpleType st : simpleTypes) {
                    String longName = getLongName(st.getSchema().getTargetNamespace(), st.getName());
                    System.out.printf("Add simple type %s%n", longName);
                    result.put(longName, st);
                }
            }
        }
    }

    /**
     * Map of elements found in schemas.
     */
    public static Map<String, Element> createMapOfElements(Definitions defs) {
        Map<String, Element> result = new TreeMap<>();
        Set<String> alreadyCheckedSchemas = new HashSet<>();
        for (Message message : defs.getMessages()) {
            for (Part part : message.getParts()) {
                Element element = part.getElement();
                if (element != null) {
                    Schema schema = element.getSchema();
                    fillMapOfElementsRecursively(result, schema, alreadyCheckedSchemas);
                }
            }
        }
        System.out.printf("Elements list: %d%n", result.size());
        return result;
    }

    public static void fillMapOfElementsRecursively(Map<String, Element> result,
                                                    Schema schema,
                                                    Set<String> alreadyCheckedSchemas) {
        if (schema != null) {
            List<Element> allElements = schema.getAllElements();
            if (allElements != null) {
                for (Element element : allElements) {
                    if (element.getRef() != null) {
                        System.out.println("Unhandled reference for element " + element.getName() + " in schema " + schema.getSchemaLocation());
                    }
                    String longName = getLongName(element.getSchema().getTargetNamespace(), element.getName());
                    result.put(longName, element);
                }
            }
            List<Schema> importedSchemas = schema.getImportedSchemas();
            if (importedSchemas != null) {
                for (Schema importedSchema : importedSchemas) {
                    if (!alreadyCheckedSchemas.contains(importedSchema.getSchemaLocation())) {
                        alreadyCheckedSchemas.add(importedSchema.getSchemaLocation());
                        fillMapOfElementsRecursively(result, importedSchema, alreadyCheckedSchemas);
                    }
                }
            }
        }
    }

}
