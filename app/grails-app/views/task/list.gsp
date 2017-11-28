<%@ page import="com.k_int.kbplus.Task" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'task.label', default: 'Task')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><g:message code="default.list.label" args="[entityName]" /></h1>

		<g:if test="${flash.message}">
			<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
		</g:if>

		<div class="ui grid">

			<div class="twelve wide column">

				<table class="ui celled striped table">
					<thead>
						<tr>
						
							<th class="header"><g:message code="task.license.label" default="License" /></th>
						
							<th class="header"><g:message code="task.org.label" default="Org" /></th>
						
							<th class="header"><g:message code="task.pkg.label" default="Pkg" /></th>
						
							<th class="header"><g:message code="task.subscription.label" default="Subscription" /></th>
						
							<g:sortableColumn property="title" title="${message(code: 'task.title.label', default: 'Title')}" />
						
							<g:sortableColumn property="endDate" title="${message(code: 'task.endDate.label', default: 'End Date')}" />
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${taskInstanceList}" var="taskInstance">
						<tr>
						
							<td>${fieldValue(bean: taskInstance, field: "license")}</td>
						
							<td>${fieldValue(bean: taskInstance, field: "org")}</td>
						
							<td>${fieldValue(bean: taskInstance, field: "pkg")}</td>
						
							<td>${fieldValue(bean: taskInstance, field: "subscription")}</td>
						
							<td>${fieldValue(bean: taskInstance, field: "title")}</td>
						
							<td>${fieldValue(bean: taskInstance, field: "endDate")}</td>
						
							<td class="link">
								<g:link action="show" id="${taskInstance.id}" class="btn btn-small">Show &raquo;</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${taskInstanceTotal}" />
				</div>
			</div><!-- .twelve -->

			<div class="four wide column">
				<div class="well">
					<ul class="nav nav-list">
						<li class="nav-header">${entityName}</li>
						<li class="active">
							<g:link class="list" action="list">
								<i class="icon-list icon-white"></i>
								<g:message code="default.list.label" args="[entityName]" />
							</g:link>
						</li>
						<li>
							<g:link class="create" action="create">
								<i class="icon-plus"></i>
								<g:message code="default.create.label" args="[entityName]" />
							</g:link>
						</li>
					</ul>
				</div>
			</div><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
