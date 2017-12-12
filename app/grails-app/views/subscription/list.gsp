
<%@ page import="com.k_int.kbplus.Subscription" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'subscription.label', default: 'Subscription')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div>

				<h1 class="ui header"><g:message code="default.list.label" args="[entityName]" /></h1>

			<semui:messages data="${flash}" />
				
				<table class="ui celled striped table">
					<thead>
						<tr>
  						        <th class="header">Subscription Name</th>
                                                        <th class="header">Subscription Type</th>
                                                        <th class="header">Subscriber</th>
							<g:sortableColumn property="startDate" title="${message(code: 'subscription.startDate.label', default: 'Start Date')}" />
							<g:sortableColumn property="endDate" title="${message(code: 'subscription.endDate.label', default: 'End Date')}" />
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${subscriptionInstanceList}" var="subscriptionInstance">
						<tr>
							<td>${fieldValue(bean: subscriptionInstance, field: "name")}</td>

						        <td><g:if test="${subscriptionInstance.instanceOf}">Subscription Taken</g:if><g:else>Subscription Offered</g:else></td>

							<td>${subscriptionInstance?.subscriber?.name}</td>

							<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${subscriptionInstance.startDate}" /></td>
						
							<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}"  date="${subscriptionInstance.endDate}" /></td>
						
							<td class="link">
								<g:link action="show" id="${subscriptionInstance.id}" class="ui tiny button">Show</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>

					<semui:paginate total="${subscriptionInstanceTotal}" />

			
		</div>
	</body>
</html>
