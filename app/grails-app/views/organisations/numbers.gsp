<%@ page
        import="com.k_int.kbplus.Org"
        import="com.k_int.kbplus.Person"
        import="com.k_int.kbplus.PersonRole"
        import="com.k_int.kbplus.RefdataValue"
        import="com.k_int.kbplus.RefdataCategory"
%>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
    <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.show.label" args="[entityName]" /></title>
</head>
<body>

<g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

<h1 class="ui header"><semui:headerIcon />
${orgInstance.name}
</h1>

<g:render template="nav" contextPath="." />

<semui:messages data="${flash}" />


<g:if test="${editable}">
    <input class="ui button"
           value="${message(code: 'numbers.create.label')}"
           data-semui="modal"
           href="#numbersFormModal" />
</g:if>

<g:render template="/numbers/formModal" model="['tenant': contextOrg,
                                               'org': orgInstance,
                                               'isPublic': RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'No'),
                                               presetFunctionType: RefdataValue.getByValueAndCategory('General contact person', 'Person Function'),
                                               tmplHideResponsibilities: true]"/>

<g:if test="${visiblePersons}">
    <h5 class="ui header"><g:message code="org.prsLinks.label" default="Persons" /></h5>

    <g:render template="/templates/cpa/person_table" model="${[persons: visiblePersons]}" />
</g:if>


</body>
</html>