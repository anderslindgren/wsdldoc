package com.tsystems.wsdldoc;

import java.util.List;
import java.util.Objects;

/**
 * By: Alexey Matveev
 * Date: 25.08.2016
 * Time: 17:20
 */
public final class ServiceData {
    private final String name;
    private final List<MethodData> methods;

    /**
     *
     */
    public ServiceData(String name, List<MethodData> methods) {
        this.name = name;
        this.methods = methods;
    }

    public String getName() {
        return name;
    }

    public List<MethodData> getMethods() {
        return methods;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ServiceData) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.methods, that.methods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, methods);
    }

    @Override
    public String toString() {
        return "ServiceData[" +
                "name=" + name + ", " +
                "methods=" + methods + ']';
    }

}
