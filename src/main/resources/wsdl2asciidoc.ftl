= ${title}

== Services

<#list services as service>
=== ${service.name}

==== Methods
<#list service.methods as method>

===== ${method.name}

// tag::${method.name}[]
*Request: ${method.request.name}*

<#if method.request.description ??>
Description: ${method.request.description}

</#if>
<#if method.request.sequence??>
[cols="3,3,1,8"]
|===
|Parameter |Datatype |Cardinality |Description

<#list method.request.sequence as type>
|${type.name}
<#if type.nativeType>
|<#if type.typeName??>${type.typeName}</#if>
<#else>
|<#if type.typeName??><<${type.typeName}>></#if>
</#if>
|${type.minOccurs}..<#if type.maxOccurs??>${type.maxOccurs}<#else>*</#if>
|<#if type.description??>${type.description}<#else>_Missing_</#if>
</#list>
|===
</#if>

*Response: ${method.response.name}*

<#if method.response.description ??>
Description: ${method.response.description}

</#if>
<#if method.response.sequence??>
[cols="3,3,1,8"]
|===
|Parameter |Datatype |Cardinality |Description

<#list method.response.sequence as type>
|${type.name}
<#if type.nativeType>
|<#if type.typeName??>${type.typeName}</#if>
<#else>
|<#if type.typeName??><<${type.typeName}>></#if>
</#if>
|${type.minOccurs}..<#if type.maxOccurs??>${type.maxOccurs}<#else>*</#if>
|<#if type.description??>${type.description}<#else>_Missing_</#if>
</#list>
|===
</#if>
// end::${method.name}[]
</#list>
</#list>

==== Datatypes
<#list types?keys as name>
<#if types[name].type == 0>

// tag::${name}[]
.${name}
****
<#if types[name].schema??><#if types[name].schema != "">link:${types[name].schema}[]</#if></#if>

<#if types[name].description ??>
${types[name].description}

</#if>
<#if types[name].sequence??>
[cols="2,2,2,4"]
|===
|Attribut |Datatyp |Cardinality |Beskrivning

<#list types[name].sequence as el>
|${el.name}
<#if el.nativeType>
<#if el.typeName??>|${el.typeName}</#if>
<#else>
|<<${el.typeName}>>
</#if>
|${el.minOccurs}..<#if el.maxOccurs??>${el.maxOccurs}<#else>*</#if>
|<#if el.description??>${el.description}<#else>_Missing_</#if>
</#list>
|===
</#if>
****
// end::${name}[]
</#if>
</#list>
<#list types?keys as name>
<#if types[name].type == 1>

// tag::${name}[]
.${name}
****
<#if types[name].schema??>
<#if types[name].schema != "">
link:${types[name].schema}[]

</#if>
</#if>
<#if types[name].description ??>
${types[name].description}

</#if>
<#if types[name].enumerations??>
<#list types[name].enumerations>
[cols="1,3"]
|===
|Värde |Beskrivning

<#items as en>
| ${en}
| *Saknar beskrivning*
</#items>
|===
</#list>
</#if>
<#if types[name].minLength??>
Min length::
${types[name].minLength}
</#if>
<#if types[name].minInclusive??>
Min inclusive::
${types[name].minInclusive}
</#if>
<#if types[name].maxLength??>
Max length::
${types[name].maxLength}
</#if>
<#if types[name].maxInclusive??>
Max inclusive::
${types[name].maxInclusive}
</#if>
<#if types[name].length??>
Length::
${types[name].length}
</#if>
<#if types[name].pattern??>
Pattern::
----
${types[name].pattern}
----
</#if>
<#if types[name].whiteSpace??>
White space::
${types[name].whiteSpace}
</#if>
<#if types[name].totalDigits??>
Total digits::
${types[name].totalDigits}
</#if>
<#if types[name].fractionDigits??>
Fraction digits::
${types[name].fractionDigits}
</#if>
****
// end::${name}[]
</#if>
</#list>

