<%@ page import="com.k_int.kbplus.OrgAccessPoint" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'accessPoint.label', default: 'Access Point')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
    <g:javascript>
      $(function() {
        $('body').attr('class', 'organisation_accessPoint_create');
     });
    </g:javascript>
	</head>
	<body>
            <div>
                <semui:breadcrumbs>
                    <semui:crumb controller="organisation" action="show" id="${orgInstance.id}" text="${orgInstance.getDesignation()}" />
                    <semui:crumb controller="organisation" action="accessPoints" id="${orgInstance.id}" message="org.nav.accessPoints"/>
                    <semui:crumb message="accessPoint.new" class="active"/>
                </semui:breadcrumbs>
                <br>
                <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />
                    <g:message code="accessPoint.new" />
                </h1>
                <g:render template="/organisation/nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

             <semui:messages data="${flash}" />

             <g:render template="create_${accessMethod}" />

		</div>
	</body>
</html>