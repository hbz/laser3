<%@ page import="com.k_int.kbplus.Platform" %>
<r:require module="annotations" />
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb controller="platform" #action="index" message="platform.show.all" />
    <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}" />
</semui:breadcrumbs>

<semui:modeSwitch controller="platform" action="show" params="${params}" />

<h1 class="ui left aligned icon header"><semui:headerIcon />

    <g:if test="${editable}"><span id="platformNameEdit"
                                   class="xEditableValue"
                                   data-type="textarea"
                                   data-pk="${platformInstance.class.name}:${platformInstance.id}"
                                   data-name="name"
                                   data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${platformInstance.name}</span>
    </g:if>
    <g:else>${platformInstance.name}</g:else>
</h1>

<semui:messages data="${flash}" />
<g:render template="nav" />
<div id="dynamicUpdate">
  <g:render template="apLinkContent" model="result" />
</div>
</body>
</html>
