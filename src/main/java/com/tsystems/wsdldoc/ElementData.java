package com.tsystems.wsdldoc;

import java.util.Objects;

/**
 * By: Alexey Matveev
 * Date: 25.08.2016
 * Time: 19:45
 */
public class ElementData {

    private String name;

    private String typeName;

    private boolean nativeType;

    private Integer minOccurs;

    private Integer maxOccurs;

    private String description;

    private String schema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public boolean isNativeType() {
        return nativeType;
    }

    public void setNativeType(boolean nativeType) {
        this.nativeType = nativeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ElementData that = (ElementData) o;
        return nativeType == that.nativeType
                && Objects.equals(name, that.name)
                && Objects.equals(typeName, that.typeName)
                && Objects.equals(minOccurs, that.minOccurs)
                && Objects.equals(maxOccurs, that.maxOccurs)
                && Objects.equals(description, that.description)
                && Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(typeName);
        result = 31 * result + Boolean.hashCode(nativeType);
        result = 31 * result + Objects.hashCode(minOccurs);
        result = 31 * result + Objects.hashCode(maxOccurs);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(schema);
        return result;
    }

    @Override
    public String toString() {
        return "ElementData{" +
                "name='" + name + '\'' +
                ", typeName='" + typeName + '\'' +
                ", nativeType=" + nativeType +
                ", minOccurs=" + minOccurs +
                ", maxOccurs=" + maxOccurs +
                ", description='" + description + '\'' +
                ", schema='" + schema + '\'' +
                '}';
    }
}
