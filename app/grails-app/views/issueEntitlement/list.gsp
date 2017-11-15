
<%@ page import="com.k_int.kbplus.IssueEntitlement" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'issueEntitlement.label', default: 'IssueEntitlement')}" />
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
						
							<th class="header"><g:message code="issueEntitlement.status.label" default="Status" /></th>
						
							<th class="header"><g:message code="issueEntitlement.subscription.label" default="Subscription" /></th>
						
							<th class="header"><g:message code="issueEntitlement.tipp.label" default="Tipp" /></th>
						
							<g:sortableColumn property="startDate" title="${message(code: 'issueEntitlement.startDate.label', default: 'Start Date')}" />
						
							<g:sortableColumn property="startVolume" title="${message(code: 'issueEntitlement.startVolume.label', default: 'Start Volume')}" />
						
							<g:sortableColumn property="startIssue" title="${message(code: 'issueEntitlement.startIssue.label', default: 'Start Issue')}" />
						
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
								<g:link action="show" id="${issueEntitlementInstance.id}" class="ui tiny button">Show &raquo;</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${issueEntitlementInstanceTotal}" />
				</div>

		</div>
	</body>
</html>
