<%@ page import="de.laser.Platform" %>

<g:set var="entityName" value="${message(code: 'platform.label')}" />
<laser:htmlStart text="${message(code:"default.show.label", args:[entityName])}" />

<semui:modeSwitch controller="platform" action="show" params="${params}" />

<semui:breadcrumbs>
    <semui:crumb controller="platform" #action="index" message="platform.show.all" />
    <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}" />
</semui:breadcrumbs>

<semui:h1HeaderWithIcon>
    <g:if test="${editable}"><span id="platformNameEdit"
                                   class="xEditableValue"
                                   data-type="textarea"
                                   data-pk="${platformInstance.class.name}:${platformInstance.id}"
                                   data-name="name"
                                   data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${platformInstance.name}</span>
    </g:if>
    <g:else>${platformInstance.name}</g:else>
</semui:h1HeaderWithIcon>

<semui:messages data="${flash}" />
<%--<laser:render template="nav" />--%>
<div id="dynamicUpdate">
  <laser:render template="apLinkContent" model="result" />
</div>

<laser:htmlEnd />
