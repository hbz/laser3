<%@ page import="de.laser.oap.OrgAccessPoint" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'accessPoint.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.edit.label" args="[entityName]" /></title>
	</head>

<body>
<laser:script file="${this.getGroovyPageFileName()}">
    $('body').attr('class', 'organisation_accessPoint_create');
</laser:script>

  <semui:breadcrumbs>
    <semui:crumb controller="organisation" action="show" id="${orgInstance.id}" text="${orgInstance.getDesignation()}"/>
    <semui:crumb controller="organisation" action="accessPoints" id="${orgInstance.id}" message="org.nav.accessPoints"/>
    <semui:crumb message="accessPoint.new" class="active"/>
  </semui:breadcrumbs>

    <semui:h1HeaderWithIcon text="${orgInstance.name}" />

  <laser:render template="/organisation/nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, tmplAccessPointsActive: true]}"/>

  <h2 class="ui header la-noMargin-top"><g:message code="accessPoint.new"/></h2>
  <semui:messages data="${flash}"/>

  <div id="details">
    <laser:render template="createAccessPoint" model="[accessMethod: accessMethod, availableOptions : availableOptions]"/>
  </div>

</body>
</html>