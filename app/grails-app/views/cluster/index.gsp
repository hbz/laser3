
<%@ page import="com.k_int.kbplus.Cluster" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'cluster.label', default: 'Cluster')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-cluster" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-cluster" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<th><g:message code="cluster.owner.label" default="Owner" /></th>
					
						<g:sortableColumn property="definition" title="${message(code: 'cluster.definition.label', default: 'Definition')}" />
					
						<g:sortableColumn property="name" title="${message(code: 'cluster.name.label', default: 'Name')}" />
					
						<th><g:message code="cluster.type.label" default="Type" /></th>
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${clusterInstanceList}" status="i" var="clusterInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${clusterInstance.id}">${fieldValue(bean: clusterInstance, field: "owner")}</g:link></td>
					
						<td>${fieldValue(bean: clusterInstance, field: "definition")}</td>
					
						<td>${fieldValue(bean: clusterInstance, field: "name")}</td>
					
						<td>${fieldValue(bean: clusterInstance, field: "type")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${clusterInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
