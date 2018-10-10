
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
			<h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="default.list.label" args="[entityName]" /></h1>
			<semui:messages data="${flash}" />
			 <table class="ui celled la-table table">
			<thead>
					<tr>
					
						<g:sortableColumn property="definition" title="${message(code: 'cluster.definition.label', default: 'Definition')}" />
					
						<g:sortableColumn property="name" title="${message(code: 'cluster.name.label', default: 'Name')}" />
					
						<th>${com.k_int.kbplus.RefdataCategory.findByDesc('Cluster Type').getI10n('desc')}</th>
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${clusterInstanceList}" status="i" var="clusterInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${clusterInstance.id}">${fieldValue(bean: clusterInstance, field: "definition")}</g:link></td>
					
						<td>${fieldValue(bean: clusterInstance, field: "name")}</td>
					
						<td>${fieldValue(bean: clusterInstance, field: "type")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>

				<semui:paginate total="${clusterInstanceCount ?: 0}" />

		</div>
	</body>
</html>
