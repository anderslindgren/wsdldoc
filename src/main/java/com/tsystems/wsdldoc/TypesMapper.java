package com.tsystems.wsdldoc;

import com.predic8.schema.*;
import com.predic8.schema.restriction.BaseRestriction;
import com.predic8.schema.restriction.facet.*;
import groovy.namespace.QName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        complexTypeData.setDescription(getTypeDescription(ct));
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
                if (component instanceof Element p) {
                    // some parts can have "ref" but no "type"
                    ElementData ed = null;
                    String name = p.getName();
                    QName type = p.getType();
                    QName ref = p.getRef();
                    if (type != null) {
                        TypeDefinition typeDefinition = mapOfOriginalTypes.get(type.getLocalPart());
                        if (typeDefinition != null) {
                            ed = map2Element(p, typeDefinition);
                            // checks whether the type is embedded like "string", "int", or custom one
                            ed.setNativeType(false);
                        } else {
                            ed = new ElementData();
                            ed.setNativeType(true);
                            ed.setName(name);
                            ed.setTypeName(type.getLocalPart());
                            ed.setMinOccurs(mapOccurs(p.getMinOccurs()));
                            ed.setMaxOccurs(mapOccurs(p.getMaxOccurs()));
                            ed.setDescription(null);
                            Schema schema = p.getSchema();
                            if (schema != null) {
                                ed.setSchema(schema.getSchemaLocation());
                            }

                        }
                    } else if (ref != null) {
                        Element el = mapOfElements.get(ref.getLocalPart());
                        if (el != null) {
                            ed = map2Element(el, mapOfElements);
                            // checks whether the type is embedded like "string", "int", or custom one
                            ed.setNativeType(el.getType() == null);
                        } else {
                            System.out.println("Cannot find element " + ref.getLocalPart() + " in schemas for mapping");
                        }
                    } else if (p.getEmbeddedType() != null) {
                        TypeDefinition embeddedType = p.getEmbeddedType();
                        if (embeddedType instanceof SimpleType simpleType) {
                            ed = map2ElementWithSimpleType(p, simpleType);
                        } else {
                            System.out.println("Unhandled embedded type's type in component " + name);
                        }
                    } else {
                        ed = map2Element(p, mapOfElements);
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
                case EnumerationFacet enumeration -> result.getEnumerations().add(enumeration.getValue());
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
        if (element.getType() != null) {
            result.setTypeName(element.getType().getLocalPart());
        } else if (element.getRef() != null) {
            String refName = element.getRef().getLocalPart();
            result.setName(refName);
            Element refElement = mapOfElements.get(refName);
            result.setTypeName(refElement.getType().getLocalPart());
        }
        result.setMinOccurs(mapOccurs(element.getMinOccurs()));
        result.setMaxOccurs(mapOccurs(element.getMaxOccurs()));
        result.setDescription(getAnnotationDescription(element.getAnnotation()));
        Schema schema = element.getSchema();
        if (schema != null) {
            result.setSchema(schema.getSchemaLocation());
        }
        return result;
    }

    public static ElementData map2Element(Element element, TypeDefinition type) {
        ElementData result = new ElementData();
        result.setName(element.getName());
        if (type != null) {
            result.setTypeName(type.getName());
        }
        result.setMinOccurs(mapOccurs(element.getMinOccurs()));
        result.setMaxOccurs(mapOccurs(element.getMaxOccurs()));
        result.setDescription(getTypeDescription(type));
        if (type != null) {
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
        result.setDescription(getTypeDescription(type));
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

    public static String getTypeDescription(TypeDefinition type) {
        if (type == null) {
            return null;
        }
        return getAnnotationDescription(type.getAnnotation());
    }

    public static String getAnnotationDescription(Annotation annotation) {
        if (annotation != null) {
            Object contents = annotation.getContents();
            if (contents instanceof Collection) {
                for (Object next : (Collection) contents) {
                    if (next instanceof Documentation) {
                        return ((Documentation) next).getContent();
                    }
                }
            }
        }
        return null;
    }

}
