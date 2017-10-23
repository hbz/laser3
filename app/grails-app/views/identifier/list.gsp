
<%@ page import="com.k_int.kbplus.Identifier" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'identifier.label', default: 'Identifier')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="container">
				
				<div class="page-header">
					<h1><g:message code="default.list.label" args="[entityName]" /></h1>
				</div>

				<g:if test="${flash.message}">
				<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>
				
				<table class="ui celled striped table">
					<thead>
						<tr>
						
							<th class="header"><g:message code="identifier.ns.label" default="Ns" /></th>
						
							<g:sortableColumn property="value" title="${message(code: 'identifier.value.label', default: 'Value')}" />
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${identifierInstanceList}" var="identifierInstance">
						<tr>
						
							<td>${fieldValue(bean: identifierInstance, field: "ns")}</td>
						
							<td>${fieldValue(bean: identifierInstance, field: "value")}</td>
						
							<td class="link">
								<g:link action="show" id="${identifierInstance.id}" class="ui tiny button">Show &raquo;</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${identifierInstanceTotal}" />
				</div>

		</div>
	</body>
</html>
