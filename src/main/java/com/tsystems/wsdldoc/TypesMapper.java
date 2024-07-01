package com.tsystems.wsdldoc;

import com.predic8.schema.*;
import com.predic8.schema.restriction.BaseRestriction;
import com.predic8.schema.restriction.facet.*;
import groovy.namespace.QName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * By: Alexey Matveev
 * Date: 25.08.2016
 * Time: 19:28
 */
public class TypesMapper {

    public static ComplexTypeData map2ComplexType(ComplexType ct,
                                                  Map<String, TypeDefinition> mapOfOriginalTypes,
                                                  Map<String, Element> mapOfElements) {
        ComplexTypeData complexTypeData = new ComplexTypeData();
        complexTypeData.setName(ct.getName());
        complexTypeData.setSchema(ct.getSchema().getSchemaLocation());
        complexTypeData.setDescription(getDescription(ct));
        Sequence sequence = ct.getSequence();
        if (sequence != null) {
            List<SchemaComponent> particles = sequence.getParticles();
            complexTypeData.setSequence(mapComplexTypeParticles(particles, mapOfOriginalTypes, mapOfElements));
        }
        // extensions
        List<QName> superTypes = ct.getSuperTypes();
        if (superTypes != null) {
            for (QName superTypeName : superTypes) {
                TypeDefinition superType = mapOfOriginalTypes.get(superTypeName.getLocalPart());
                if (superType != null) {
                    List<String> superTypesList = complexTypeData.getSuperTypes();
                    if (superTypesList == null) {
                        superTypesList = new ArrayList<>();
                        complexTypeData.setSuperTypes(superTypesList);
                    }
                    superTypesList.add(superType.getName());
                }
            }
        }

        /* case when type extends some other type and also has additional sequence
        <xs:complexType name="S_SubOrderContact">
            <xs:complexContent>
                <xs:extension base="suborder:S_SubOrder">
                    <xs:sequence>
                        <xs:element maxOccurs="1" minOccurs="1" name="contact" type="contact:S_Contact"/>
                    </xs:sequence>
                </xs:extension>
            </xs:complexContent>
        </xs:complexType>
         */
        SchemaComponent model = ct.getModel();
        if (model instanceof ComplexContent complexContent) {
            Derivation derivation = complexContent.getDerivation();
            if (derivation instanceof Extension extension) {
                SchemaComponent extensionModel = extension.getModel();
                if (extensionModel instanceof Sequence extensionModelSequence) {
                    List<SchemaComponent> particles = extensionModelSequence.getParticles();
                    // add extension sequence to the base sequence if exist
                    List<ElementData> extensionSequenceMapped =
                            mapComplexTypeParticles(particles, mapOfOriginalTypes, mapOfElements);
                    if (extensionSequenceMapped != null) {
                        List<ElementData> existingSequence = complexTypeData.getSequence();
                        if (existingSequence != null) {
                            existingSequence.addAll(extensionSequenceMapped);
                        } else {
                            complexTypeData.setSequence(extensionSequenceMapped);
                        }
                    }
                }
            }
        }
        return complexTypeData;
    }

    public static List<ElementData> mapComplexTypeParticles(List<SchemaComponent> components,
                                                            Map<String, TypeDefinition> mapOfOriginalTypes,
                                                            Map<String, Element> mapOfElements) {
        List<ElementData> sequenceResult = null;
        if (components != null) {
            for (SchemaComponent component : components) {
                if (component instanceof Element element) {
                    // some parts can have "ref" but no "type"
                    ElementData ed = null;
                    String name = element.getName();
                    QName type = element.getType();
                    QName ref = element.getRef();
                    if (type != null) {
                        String longName = getLongName(type.getNamespaceURI(), type.getLocalPart());
                        TypeDefinition typeDefinition = mapOfOriginalTypes.get(longName);
//                        TypeDefinition typeDefinition = mapOfOriginalTypes.get(type.getLocalPart());
                        // checks whether the type is embedded like "string", "int", or custom one
                        if (typeDefinition != null) {
                            ed = map2Element(element, typeDefinition);
                            ed.setNativeType(false);
                        } else {
                            ed = new ElementData();
                            ed.setNativeType(true);
                            ed.setName(name);
                            ed.setTypeName(type.getLocalPart());
                            ed.setMinOccurs(mapOccurs(element.getMinOccurs()));
                            ed.setMaxOccurs(mapOccurs(element.getMaxOccurs()));
                            ed.setDescription(getDescription(element));
                            Schema schema = element.getSchema();
                            if (schema != null) {
                                ed.setSchema(schema.getSchemaLocation());
                            }

                        }
                    } else if (ref != null) {
                        String namespaceURI = ref.getNamespaceURI();
                        String longName = getLongName(namespaceURI, ref.getLocalPart());

                        Element el = mapOfElements.get(longName);
                        if (el != null) {
                            ed = map2Element(el, mapOfElements);
                            // checks whether the type is embedded like "string", "int", or custom one
                            ed.setNativeType(el.getType() == null);
                        } else {
                            System.out.println("Cannot find element " + longName + " in schemas for mapping");
                        }
                    } else if (element.getEmbeddedType() != null) {
                        TypeDefinition embeddedType = element.getEmbeddedType();
                        if (embeddedType instanceof SimpleType simpleType) {
                            ed = map2ElementWithSimpleType(element, simpleType);
//                        } else if (embeddedType instanceof ComplexType complexType) {
//                            ed = map2Element(element, complexType);
                        } else {
                            System.out.println("Unhandled embedded type's type in component " + name + " " + embeddedType);
                        }
                    } else {
                        ed = map2Element(element, mapOfElements);
                    }
                    if (ed != null) {
                        if (sequenceResult == null) {
                            sequenceResult = new ArrayList<>();
                        }
                        sequenceResult.add(ed);
                    }
                } else {
                    System.out.println("Particle is of some other type: " + component.getClass().getName());
                }
            }
        }
        return sequenceResult;
    }

    public static SimpleTypeData map2SimpleType(SimpleType st) {
        SimpleTypeData result = new SimpleTypeData();
        result.setName(st.getName());
        result.setSchema(st.getSchema().getSchemaLocation());
        result.setDescription(getDescription(st));
        BaseRestriction restriction = st.getRestriction();
        if (restriction != null) {
            result.setBase(restriction.getBase().getLocalPart());
            List<Facet> facets = restriction.getFacets();
            if (facets != null) {
                mapFacets(st, facets, result);
            }
        }
        return result;
    }

    private static void mapFacets(SimpleType st, List<Facet> facets, SimpleTypeData result) {
        for (Facet facet : facets) {
            switch (facet) {
                case EnumerationFacet enumeration -> result.addEnum(new EnumType(enumeration));
                case MinLengthFacet minLength -> result.setMinLength(minLength.getValue());
                case MaxLengthFacet maxLength -> result.setMaxLength(maxLength.getValue());
                case LengthFacet length -> result.setLength(length.getValue());
                case PatternFacet pattern -> result.setPattern(pattern.getValue());
                case TotalDigitsFacet totalDigits -> result.setTotalDigits(totalDigits.getValue());
                case WhiteSpaceFacet whiteSpace -> result.setWhiteSpace(whiteSpace.getValue());
                case MinInclusiveFacet minInclusive -> result.setMinInclusive(minInclusive.getValue());
                case MinExclusiveFacet minExclusive -> result.setMinExclusive(minExclusive.getValue());
                case MaxInclusiveFacet maxInclusive -> result.setMaxInclusive(maxInclusive.getValue());
                case MaxExclusiveFacet maxExclusive -> result.setMaxExclusive(maxExclusive.getValue());
                case FractionDigits fraction -> result.setFractionDigits(fraction.getValue());
                case null -> System.out.println("Facet type: null for simple type: " + st.getName());
                default -> System.out.println("Unhandled facet type: " + facet.getName() +
                                                      " for simple type: " + st.getName());
            }
        }
    }

    public static ElementData map2Element(Element element, Map<String, Element> mapOfElements) {
        ElementData result = new ElementData();

        result.setName(element.getName() == null ? "" : element.getName());
        QName type = element.getType();
        if (type != null) {
            String namespace = type.getNamespaceURI();
            String localPart = type.getLocalPart();
            String longName;
            if (namespace.equals("http://www.w3.org/2001/XMLSchema")) {
                longName = localPart;
                result.setNativeType(true);
            } else {
                longName = getLongName(namespace, localPart);
                result.setNativeType(false);
            }
            result.setTypeName(longName);
        } else if (element.getEmbeddedType() != null) {
            TypeDefinition embeddedType = element.getEmbeddedType();
            if (embeddedType instanceof SimpleType st) {
                BaseRestriction restriction = st.getRestriction();
                if (restriction != null) {
                    QName base = restriction.getBase();
                    result.setNativeType(base.getNamespaceURI().contains("www.w3.org"));
                    result.setTypeName(base.getLocalPart());
                }
            } else if (embeddedType instanceof ComplexType ct) {
                result.setTypeName(ct.getBuildInTypeName());
            }
        } else if (element.getRef() != null) {
            String namespace = element.getSchema().getTargetNamespace();
            String refName = element.getRef().getLocalPart();
            String longName = getLongName(namespace, refName);
            result.setName(refName);
            Element refElement = mapOfElements.get(longName);
            if (refElement != null) {
                result.setTypeName(refElement.getType().getLocalPart());
            }
        }
        result.setMinOccurs(mapOccurs(element.getMinOccurs()));
        result.setMaxOccurs(mapOccurs(element.getMaxOccurs()));
        result.setDescription(getDescription(element));
        Schema schema = element.getSchema();
        if (schema != null) {
            result.setSchema(schema.getSchemaLocation());
        }
        return result;
    }

    public static String getLongName(String namespace, String localPart) {
        int i = namespace.indexOf("schema/");
        String start = namespace;
        if (i >= 0) {
            start = namespace.substring(i + 7);
        }
        return start.replaceAll("/", "_") + "_" + localPart;
    }

    public static ElementData map2Element(Element element, TypeDefinition type) {
        ElementData result = new ElementData();
        result.setName(element.getName());
        result.setMinOccurs(mapOccurs(element.getMinOccurs()));
        result.setMaxOccurs(mapOccurs(element.getMaxOccurs()));
        result.setDescription(getDescription(type));
        if (type != null) {
            if (type.getName() != null) {
                result.setTypeName(type.getName());
            }
            Schema schema = type.getSchema();
            if (schema != null) {
                result.setSchema(schema.getSchemaLocation());
            }
        }
        return result;
    }

    public static ElementData map2ElementWithSimpleType(Element element, SimpleType type) {
        ElementData result = new ElementData();
        result.setName(element.getName());
        result.setTypeName(type.getBuildInTypeName());
        result.setNativeType(true);
        result.setMinOccurs(mapOccurs(element.getMinOccurs()));
        result.setMaxOccurs(mapOccurs(element.getMaxOccurs()));
        result.setDescription(getDescription(type));
        Schema schema = type.getSchema();
        if (schema != null) {
            result.setSchema(schema.getSchemaLocation());
        }
        return result;
    }

    public static Integer mapOccurs(String occurs) {
        if (occurs.equals("unbounded")) {
            return null;
        }
        return Integer.valueOf(occurs);
    }

    public static String getDescription(TypeDefinition type) {
        if (type == null) {
            return null;
        }
        return getAnnotationDescription(type.getAnnotation());
    }

    private static String getDescription(Element element) {
        if (element == null) {
            return null;
        }
        return getAnnotationDescription(element.getAnnotation());
    }

    public static String getAnnotationDescription(Annotation annotation) {
        if (annotation != null) {
            List<Documentation> docs = annotation.getDocumentations();
            if (docs != null) {
                return docs.stream()
                           .filter(d -> d.getLang() != null && d.getLang().equals("sv"))
                           .map(documentation -> documentation.getContent().strip().stripIndent())
                           .collect(Collectors.joining("\n"));
            }
        }
        return null;
    }
}
