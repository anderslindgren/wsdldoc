package com.tsystems.wsdldoc;

/**
 * By: Alexey Matveev
 * Date: 25.08.2016
 * Time: 17:20
 */
public record MethodData(String name,
                         String refName,
                         ComplexTypeData request,
                         ComplexTypeData response) {
}
