<%@ page import="de.laser.Platform" %>

<g:set var="entityName" value="${message(code: 'platform.label')}" />
<laser:htmlStart text="${message(code:"default.show.label", args:[entityName])}" />

<ui:modeSwitch controller="platform" action="show" params="${params}" />

<ui:breadcrumbs>
    <ui:crumb controller="platform" #action="index" message="platform.show.all" />
    <ui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}" />
</ui:breadcrumbs>

<ui:h1HeaderWithIcon>
    <g:if test="${editable}"><span id="platformNameEdit"
                                   class="xEditableValue"
                                   data-type="textarea"
                                   data-pk="${platformInstance.class.name}:${platformInstance.id}"
                                   data-name="name"
                                   data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${platformInstance.name}</span>
    </g:if>
    <g:else>${platformInstance.name}</g:else>
</ui:h1HeaderWithIcon>

<ui:messages data="${flash}" />
<%--<laser:render template="nav" />--%>
<div id="dynamicUpdate">
  <laser:render template="apLinkContent" model="result" />
</div>

<laser:htmlEnd />
