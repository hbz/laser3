
<%@ page import="com.k_int.kbplus.Task" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'task.label', default: 'Task')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-task" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-task" class="content scaffold-list" role="main">
			<h1 class="ui header"><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<th><g:message code="task.license.label" default="License" /></th>
					
						<th><g:message code="task.org.label" default="Org" /></th>
					
						<th><g:message code="task.pkg.label" default="Pkg" /></th>
					
						<th><g:message code="task.subscription.label" default="Subscription" /></th>
					
						<g:sortableColumn property="title" title="${message(code: 'task.title.label', default: 'Title')}" />
					
						<g:sortableColumn property="description" title="${message(code: 'task.description.label', default: 'Description')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${taskInstanceList}" status="i" var="taskInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${taskInstance.id}">${fieldValue(bean: taskInstance, field: "license")}</g:link></td>
					
						<td>${fieldValue(bean: taskInstance, field: "org")}</td>
					
						<td>${fieldValue(bean: taskInstance, field: "pkg")}</td>
					
						<td>${fieldValue(bean: taskInstance, field: "subscription")}</td>
					
						<td>${fieldValue(bean: taskInstance, field: "title")}</td>
					
						<td>${fieldValue(bean: taskInstance, field: "description")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${taskInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
