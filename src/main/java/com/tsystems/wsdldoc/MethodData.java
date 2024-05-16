package com.tsystems.wsdldoc;

/**
 * By: Alexey Matveev
 * Date: 25.08.2016
 * Time: 17:20
 */
public final class MethodData {

    private final String name;

    private final ComplexTypeData request;

    private final ComplexTypeData response;

    public MethodData(String name, ComplexTypeData request, ComplexTypeData response) {
        this.name = name;
        this.request = request;
        this.response = response;
    }

    public String getName() {
        return name;
    }

    public ComplexTypeData getRequest() {
        return request;
    }

    public ComplexTypeData getResponse() {
        return response;
    }
}
