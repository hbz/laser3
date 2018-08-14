<%@ page import="com.k_int.kbplus.Task" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'task.label', default: 'Task')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
        <h1 class="ui header"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>

        <g:if test="${flash.message}">
            <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </g:if>


				<dl>
					<g:if test="${taskInstance?.license}">
						<dt><g:message code="task.license.label" default="License" /></dt>
						<dd><g:link controller="licenseDetails" action="show" id="${taskInstance?.license?.id}">${taskInstance?.license}</g:link></dd>
					</g:if>
					<g:if test="${taskInstance?.org}">
						<dt><g:message code="task.org.label" default="Org" /></dt>
						<dd><g:link controller="organisations" action="show" id="${taskInstance?.org?.id}">${taskInstance?.org}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.pkg}">
						<dt><g:message code="task.pkg.label" default="Pkg" /></dt>
						<dd><g:link controller="packageDetails" action="show" id="${taskInstance?.pkg?.id}">${taskInstance?.pkg}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.subscription}">
						<dt><g:message code="task.subscription.label" default="Subscription" /></dt>
						<dd><g:link controller="subscriptionDetails" action="show" id="${taskInstance?.subscription?.id}">${taskInstance?.subscription}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.title}">
						<dt><g:message code="task.title.label" default="Title" /></dt>
						<dd><g:fieldValue bean="${taskInstance}" field="title"/></dd>
					</g:if>
				
					<g:if test="${taskInstance?.description}">
						<dt><g:message code="task.description.label" default="Description" /></dt>
						<dd><g:fieldValue bean="${taskInstance}" field="description"/></dd>
					</g:if>
				
					<g:if test="${taskInstance?.status}">
						<dt><g:message code="task.status.label" default="Status" /></dt>
						<dd><g:link controller="refdataValue" action="show" id="${taskInstance?.status?.id}">${taskInstance?.status}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.creator}">
						<dt><g:message code="task.creator.label" default="Creator" /></dt>
						<dd><g:link controller="userDetails" action="show" id="${taskInstance?.creator?.id}">${taskInstance?.creator?.display}</g:link></dd>
					</g:if>

					<g:if test="${taskInstance?.createDate}">
						<dt><g:message code="task.createDate.label" default="Create Date" /></dt>
						<dd><g:formatDate date="${taskInstance?.createDate}" /></dd>
					</g:if>

					<g:if test="${taskInstance?.endDate}">
						<dt><g:message code="task.endDate.label" default="End Date" /></dt>
						<dd><g:formatDate date="${taskInstance?.endDate}" /></dd>
					</g:if>
				
					<g:if test="${taskInstance?.responsibleUser}">
						<dt><g:message code="task.responsibleUser.label" default="Responsible User" /></dt>
						<dd><g:link controller="userDetails" action="show" id="${taskInstance?.responsibleUser?.id}">${taskInstance?.responsibleUser?.display}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.responsibleOrg}">
						<dt><g:message code="task.responsibleOrg.label" default="Responsible Org" /></dt>
						<dd><g:link controller="organisations" action="show" id="${taskInstance?.responsibleOrg?.id}">${taskInstance?.responsibleOrg}</g:link></dd>
					</g:if>
				</dl>

				<g:form>
					<g:hiddenField name="id" value="${taskInstance?.id}" />
					<div class="ui form-actions">
						<g:link class="ui button" action="edit" id="${taskInstance?.id}">
							<i class="write icon"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="ui negative button" type="submit" name="_action_delete">
							<i class="trash alternate icon"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
				</g:form>
	</body>
</html>
