= ${title}
:icons:
:icontype: svg

== Services

<#list services as service>
=== ${service.name}

==== Methods
<#list service.methods as method>

===== ${method.name}

// tag::${method.refName}[]
*Request: ${method.request.name}*

<#if method.request.description ??>
Description: ${method.request.description}

</#if>
<#if method.request.sequence??>
[cols="3,3,1,8"]
|===
|Parameter |Datatyp |Kardinalitet |Beskrivning

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
|Parameter |Datatyp |Kardinalitet |Beskrivning

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
// end::${method.refName}[]
</#list>
</#list>

==== Datatypes
<#list types?keys as key>
<#if types[key].type == 0>

// tag::${key}[]
[[${key},${types[key].name}]]
.${types[key].name} icon:ct[]
****
<#if types[key].schema??><#if types[key].schema != "">link:${types[key].schema}[]</#if></#if>

<#if types[key].description ??>
${types[key].description}

</#if>
<#if types[key].sequence??>
[cols="2,2,2,4"]
|===
|Attribut |Datatyp |Kardinalitet |Beskrivning

<#list types[key].sequence as el>
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
// end::${key}[]
</#if>
</#list>
<#list types?keys as key>
<#if types[key].type == 1>

// tag::${key}[]
[[${key},${types[key].name}]]
.${types[key].name} icon:st[]
****
<#if types[key].schema??>
<#if types[key].schema != "">
link:${types[key].schema}[]

</#if>
</#if>
<#if types[key].description ??>
${types[key].description}

</#if>
<#if types[key].base ??>
*Base type:* ${types[key].base}

</#if>
<#if types[key].enumerations??>
*Enumeration:*
<#list types[key].enumerations>
[cols="1,3"]
|===
|Värde |Beskrivning

<#items as en>
|`${en.value}`
|<#if en.description??>${en.description}<#else>_Missing_</#if>
</#items>
|===
</#list>
</#if>
<#if types[key].minLength??>
*Min length:* ${types[key].minLength}

</#if>
<#if types[key].minInclusive??>
*Min inclusive:* ${types[key].minInclusive}

</#if>
<#if types[key].maxLength??>
*Max length:* ${types[key].maxLength}

</#if>
<#if types[key].maxInclusive??>
*Max inclusive:* ${types[key].maxInclusive}

</#if>
<#if types[key].length??>
*Length:* ${types[key].length}

</#if>
<#if types[key].pattern??>
*Pattern:*
----
${types[key].pattern}
----
</#if>
<#if types[key].whiteSpace??>
*White space:* ${types[key].whiteSpace}

</#if>
<#if types[key].totalDigits??>
*Total digits:* ${types[key].totalDigits}

</#if>
<#if types[key].fractionDigits??>
*Fraction digits:* ${types[key].fractionDigits}

</#if>
****
// end::${key}[]
</#if>
</#list>

