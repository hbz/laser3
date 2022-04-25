<%@ page import="de.laser.Platform" %>
<%-- r:require module="annotations" / --%>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <g:set var="entityName" value="${message(code: 'platform.label')}" />
    <title>${message(code:'laser')} : <g:message code="default.show.label" args="[entityName]" /></title>
</head>
<body>

<semui:modeSwitch controller="platform" action="show" params="${params}" />

<semui:breadcrumbs>
    <semui:crumb controller="platform" #action="index" message="platform.show.all" />
    <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}" />
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />

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
<%--<laser:render template="nav" />--%>
<div id="dynamicUpdate">
  <laser:render template="apLinkContent" model="result" />
</div>
</body>
</html>
