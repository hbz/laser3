
<%@ page import="com.k_int.kbplus.Cluster" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'cluster.label', default: 'Cluster')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div>
				

					<h1 class="ui header"><g:message code="default.list.label" args="[entityName]" /></h1>


			<semui:messages data="${flash}" />
				
				<table class="ui celled striped table">
					<thead>
						<tr>
						
							<g:sortableColumn property="definition" title="${message(code: 'cluster.definition.label', default: 'Definition')}" />
						
							<g:sortableColumn property="name" title="${message(code: 'cluster.name.label', default: 'Name')}" />
						
							<th class="header"><g:message code="cluster.type.label" default="Type" /></th>
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${clusterInstanceList}" var="clusterInstance">
						<tr>
						
							<td>${fieldValue(bean: clusterInstance, field: "definition")}</td>
						
							<td>${fieldValue(bean: clusterInstance, field: "name")}</td>
						
							<td>${fieldValue(bean: clusterInstance, field: "type")}</td>
						
							<td class="link">
								<g:link action="show" id="${clusterInstance.id}" class="ui tiny button">Show &raquo;</g:link>
								<g:link action="edit" id="${clusterInstance.id}" class="ui tiny button">Edit</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${clusterInstanceTotal}" />
				</div>

		</div>
	</body>
</html>
