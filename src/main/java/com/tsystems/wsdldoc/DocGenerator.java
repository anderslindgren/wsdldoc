package com.tsystems.wsdldoc;

import com.predic8.schema.*;
import com.predic8.wsdl.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import groovy.namespace.QName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.tsystems.wsdldoc.TypesLocator.*;
import static com.tsystems.wsdldoc.TypesMapper.map2Element;
import static freemarker.template.Configuration.VERSION_2_3_32;

/**
 * The main service for HTML documentation generation.
 * Takes parsed parameters, downloads and parses the input WSDL, processes the FTL.
 * <p/>
 * By: Alexey Matveev
 * Date: 31.08.2016
 * Time: 9:36
 */
public class DocGenerator {

    /**
     * Generates the documentation out of the parameters.
     *
     * @param sourceWsdlLocations - the list of WSDL URL's
     * @param outputFile          - the output file
     * @param title               - title of the document
     */
    public static void generateDoc(String[] sourceWsdlLocations, File outputFile, String title)
            throws IOException, TemplateException {

        WSDLParser parser = new WSDLParser();

        List<Definitions> defsList = Arrays.stream(sourceWsdlLocations)
                                           .map(parser::parse)
                                           .toList();

        Configuration cfg = new Configuration(VERSION_2_3_32);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        // loading templates
        cfg.setClassForTemplateLoading(DocGenerator.class, "/");

        List<ServiceData> services = new ArrayList<>();
        Map<String, TypeData> types = new HashMap<>();

        for (Definitions defs : defsList) {
            Map<String, TypeDefinition> mapOfOriginalTypes = createMapOfOriginalTypes(defs);
            Map<String, Element> mapOfElements = createMapOfElements(defs);
            types.putAll(createMapOfTypes(defs, mapOfOriginalTypes, mapOfElements));

            List<ServiceData> servicesList = defs.getPortTypes()
                                                 .stream()
                                                 .map(portType -> {
                                                     List<MethodData> methods = getMethods(portType,
                                                                                           defs,
                                                                                           mapOfOriginalTypes,
                                                                                           mapOfElements);
                                                     return new ServiceData(portType.getName(), methods);
                                                 })
                                                 .toList();
            services.addAll(servicesList);
        }

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("services", services);
        rootMap.put("types", types);
        rootMap.put("title", title);

        // process the template
        Template template = cfg.getTemplate("com/tsystems/wsdldoc/wsdldoc.ftl");

        template.process(rootMap, new FileWriter(outputFile));
    }

    private static List<MethodData> getMethods(PortType portType,
                                               Definitions defs,
                                               Map<String, TypeDefinition> mapOfOriginalTypes,
                                               Map<String, Element> mapOfElements) {

        return portType.getOperations()
                       .stream()
                       .map(operation -> {
                           // request
                           Input input = operation.getInput();
                           ComplexTypeData in = getComplexTypeData(input, defs, mapOfOriginalTypes, mapOfElements);
                           // response
                           Output output = operation.getOutput();
                           ComplexTypeData out = getComplexTypeData(output, defs, mapOfOriginalTypes, mapOfElements);

                           String name = operation.getName();
                           return new MethodData(name, in, out);
                       })
                       .toList();
    }

    public static ComplexTypeData getComplexTypeData(AbstractPortTypeMessage portTypeMessage,
                                                     Definitions defs,
                                                     Map<String, TypeDefinition> mapOfOriginalTypes,
                                                     Map<String, Element> mapOfElements) {
        String messageName = portTypeMessage.getMessagePrefixedName().getLocalName();
        String typeName = getTypeName(defs, messageName);
        TypeDefinition originalRequestType = mapOfOriginalTypes.get(typeName);
        ComplexTypeData result = null;
        if (originalRequestType != null) {
            result = TypesMapper.map2ComplexType((ComplexType) originalRequestType, mapOfOriginalTypes, mapOfElements);
        } else {
            // check if it's an element
            Element element = mapOfElements.get(typeName);
            if (element != null) {
                result = new ComplexTypeData();
                result.setName(typeName);
                TypeDefinition embeddedType = element.getEmbeddedType();
                if (embeddedType instanceof ComplexType complexType) {
                    SchemaComponent model = complexType.getModel();
                    if (model instanceof Sequence modelSequence) {
                        List<SchemaComponent> particles = modelSequence.getParticles();
                        if (particles != null) {
                            List<ElementData> sequence = getElementDataOfParticles(particles, mapOfElements);
                            result.setSequence(sequence);
                        }
                    }
                }
            }
        }
        if (result == null) {
            // fallback solution
            System.out.println("Input/Output " + messageName + " type and element " + typeName + " was not found in schemas");
            result = new ComplexTypeData();
            result.setName(typeName);
        }

        return result;
    }

    private static List<ElementData> getElementDataOfParticles(List<SchemaComponent> particles,
                                                               Map<String, Element> mapOfElements) {
        return particles.stream()
                        .filter(p -> p instanceof Element)
                        .map(p -> map2Element((Element) p, mapOfElements))
                        .toList();
    }

    private static String getTypeName(Definitions defs, String messageName) {
        return defs.getLocalMessages().stream()
                .filter(msg -> msg.getName().equals(messageName))
                .map(msg -> getTypeName(msg.getParts()))
                .findFirst()
                .orElse(messageName);
    }

    private static String getTypeName(List<Part> parts) {
        return parts.stream().map(Part::getElement)
                    .filter(Objects::nonNull)
                    .map(DocGenerator::getTypeName)
                    .findFirst()
                    .orElse(null);
    }

    private static String getTypeName(Element element) {
        String typeName;
        QName elementType = element.getType();
        QName elementRef = element.getRef();
        if (elementType != null) {
            typeName = elementType.getLocalPart();
        } else if (elementRef != null) {
            typeName = elementRef.getLocalPart();
        } else {
            typeName = element.getName();
        }
        return typeName;
    }

}
