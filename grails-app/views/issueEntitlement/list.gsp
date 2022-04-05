
<%@ page import="de.laser.IssueEntitlement" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'issueEntitlement.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>

		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.list.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />
				
				<table class="ui sortable celled la-js-responsive-table la-table table">
					<thead>
						<tr>
						
							<th class="header"><g:message code="default.status.label" /></th>
						
							<th class="header"><g:message code="default.subscription.label" /></th>
						
							<th class="header"><g:message code="tipp.label" /></th>
						
							<g:sortableColumn property="startDate" title="${message(code: 'default.startDate.label')}" />
						
							<g:sortableColumn property="startVolume" title="${message(code: 'tipp.startVolume')}" />
						
							<g:sortableColumn property="startIssue" title="${message(code: 'tipp.startIssue')}" />
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${issueEntitlementInstanceList}" var="issueEntitlementInstance">
						<tr>
						
							<td>${fieldValue(bean: issueEntitlementInstance, field: "status")}</td>
						
							<td>${fieldValue(bean: issueEntitlementInstance, field: "subscription")}</td>
						
							<td>${fieldValue(bean: issueEntitlementInstance, field: "tipp")}</td>
						
							<td><g:formatDate date="${issueEntitlementInstance.startDate}" /></td>
						
							<td>${fieldValue(bean: issueEntitlementInstance, field: "startVolume")}</td>
						
							<td>${fieldValue(bean: issueEntitlementInstance, field: "startIssue")}</td>
						
							<td class="link">
								<g:link action="show" id="${issueEntitlementInstance.id}" class="ui tiny button">${message('code':'default.button.show.label')}</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>

					<semui:paginate total="${issueEntitlementInstanceTotal}" />

	</body>
</html>
