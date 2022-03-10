<%@ page import="de.laser.Task" %>
<!doctype html>

<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'task.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.list.label" args="[entityName]" />
			<semui:totalNumber total="${taskInstanceTotal}"/>
		</h1>

		<g:if test="${flash.message}">
			<semui:msg class="warning" text="${flash.message}"/>
		</g:if>

		<div class="ui grid">

			<div class="twelve wide column">

				<table class="ui sortable celled la-js-responsive-table la-table table">
					<thead>
						<tr>
						
							<th class="header"><g:message code="license.label" /></th>
						
							<th class="header"><g:message code="task.org.label" /></th>
						
							<th class="header"><g:message code="package.label" /></th>
						
							<th class="header"><g:message code="default.subscription.label" /></th>
						
							<g:sortableColumn property="title" title="${message(code: 'task.title.label')}" />
						
							<g:sortableColumn property="endDate" title="${message(code: 'task.endDate.label')}" />

							<th class="la-action-info">${message(code:'default.actions.label')}</th>
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

							<td class="x">
								<g:link action="show" id="${taskInstance.id}"
									class="ui icon button blue la-modern-button"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
									<i aria-hidden="true" class="write icon"></i>
								</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>

					<semui:paginate total="${taskInstanceTotal}" />

			</div><!-- .twelve -->

			<aside class="four wide column">
				<div>
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
			</aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
