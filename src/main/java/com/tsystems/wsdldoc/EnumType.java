package com.tsystems.wsdldoc;

import com.predic8.schema.Annotation;
import com.predic8.schema.restriction.facet.EnumerationFacet;

public record EnumType(String value, String description) {
    public EnumType(EnumerationFacet facet) {
        this(facet.getValue(), getDoc(facet));
    }

    private static String getDoc(EnumerationFacet facet) {
        Annotation annotation = facet.getAnnotation();
        String d;
        if (annotation != null && annotation.getDocumentations() != null) {
            d = annotation.getDocumentations().getFirst().getContent();
        } else {
            d = "Missing";
        }
        return d;
    }
}
