
<%@ page import="com.k_int.kbplus.License" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'license', default: 'License')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div>
				
				<div class="page-header">
					<h1><g:message code="default.list.label" args="[entityName]" /></h1>
				</div>

				<g:if test="${flash.message}">
				<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>
				
				<table class="ui celled striped table">
					<thead>
						<tr>
						
							<td class="header"><g:message code="license.status.label" default="Status" /></td>
						
							<td class="header"><g:message code="license.type.label" default="Type" /></td>
						
							<g:sortableColumn property="reference" title="${message(code: 'license.reference.label', default: 'Reference')}" />
						
							<td class="header"><g:message code="license.concurrentUsers.label" default="Concurrent Users" /></td>
						
							<td class="header"><g:message code="license.remoteAccess.label" default="Remote Access" /></td>
						
							<td class="header"><g:message code="license.walkinAccess.label" default="Walkin Access" /></td>
						
							<td></td>
						</tr>
					</thead>
					<tbody>
					<g:each in="${licenseInstanceList}" var="licenseInstance">
						<tr>
						
							<td>${licenseInstance.status?.value}</td>
						
							<td>${licenseInstance.type?.value}</td>
						
							<td>${fieldValue(bean: licenseInstance, field: "reference")}</td>
						
							<td>${licenseInstance.concurrentUsers?.value}</td>
						
							<td>${licenseInstance.remoteAccess?.value}</td>
						
							<td>${licenseInstance.walkinAccess?.value}</td>
						
							<td class="link">
								<g:link action="show" id="${licenseInstance.id}" class="ui tiny button">Show &raquo;</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${licenseInstanceTotal}" />
				</div>

		</div>
	</body>
</html>
