<%@ page import="de.laser.oap.OrgAccessPoint" %>

<g:set var="entityName" value="${message(code: 'accessPoint.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

<laser:script file="${this.getGroovyPageFileName()}">
    $('body').attr('class', 'organisation_accessPoint_create');
</laser:script>

  <ui:breadcrumbs>
    <ui:crumb text="${orgInstance.getDesignation()}" class="active" />
  </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="${orgInstance.name}" />

  <laser:render template="/organisation/nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, tmplAccessPointsActive: true]}"/>

  <h2 class="ui header la-noMargin-top"><g:message code="accessPoint.create_${accessMethod}"/></h2>
  <ui:messages data="${flash}"/>

  <div id="details">
    <laser:render template="createAccessPoint" model="[accessMethod: accessMethod, availableOptions : availableOptions]"/>
  </div>

<laser:htmlEnd />