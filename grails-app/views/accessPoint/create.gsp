<%@ page import="de.laser.oap.OrgAccessPoint" %>

<g:set var="entityName" value="${message(code: 'accessPoint.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

<laser:script file="${this.getGroovyPageFileName()}">
    $('body').attr('class', 'organisation_accessPoint_create');
</laser:script>

  <ui:breadcrumbs>
    <ui:crumb controller="organisation" action="show" id="${orgInstance.id}" text="${orgInstance.getDesignation()}"/>
    <ui:crumb controller="organisation" action="accessPoints" id="${orgInstance.id}" message="org.nav.accessPoints"/>
    <ui:crumb message="accessPoint.new" class="active"/>
  </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="${orgInstance.name}" />

  <laser:render template="/organisation/nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, tmplAccessPointsActive: true]}"/>

  <h2 class="ui header la-noMargin-top"><g:message code="accessPoint.new"/></h2>
  <ui:messages data="${flash}"/>

  <div id="details">
    <laser:render template="createAccessPoint" model="[accessMethod: accessMethod, availableOptions : availableOptions]"/>
  </div>

<laser:htmlEnd />