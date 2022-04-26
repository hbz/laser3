<%@ page import="de.laser.Task" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'task.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>

        <g:if test="${flash.message}">
			<semui:msg class="warning" text="${flash.message}" />
        </g:if>


				<dl>
					<g:if test="${taskInstance?.license}">
						<dt><g:message code="license.label" /></dt>
						<dd><g:link controller="license" action="show" id="${taskInstance?.license?.id}">${taskInstance?.license}</g:link></dd>
					</g:if>
					<g:if test="${taskInstance?.org}">
						<dt><g:message code="task.org.label" /></dt>
						<dd><g:link controller="organisation" action="show" id="${taskInstance?.org?.id}">${taskInstance?.org}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.pkg}">
						<dt><g:message code="package.label" /></dt>
						<dd><g:link controller="package" action="show" id="${taskInstance?.pkg?.id}">${taskInstance?.pkg}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.subscription}">
						<dt><g:message code="default.subscription.label" /></dt>
						<dd><g:link controller="subscription" action="show" id="${taskInstance?.subscription?.id}">${taskInstance?.subscription}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.title}">
						<dt><g:message code="task.title.label" /></dt>
						<dd><g:fieldValue bean="${taskInstance}" field="title"/></dd>
					</g:if>
				
					<g:if test="${taskInstance?.description}">
						<dt><g:message code="default.description.label" /></dt>
						<dd><g:fieldValue bean="${taskInstance}" field="description"/></dd>
					</g:if>
				
					<g:if test="${taskInstance?.status}">
						<dt><g:message code="task.status.label" /></dt>
						<dd><g:link controller="refdataValue" action="show" id="${taskInstance?.status?.id}">${taskInstance?.status}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.creator}">
						<dt><g:message code="task.creator.label" /></dt>
						<dd><g:link controller="user" action="show" id="${taskInstance?.creator?.id}">${taskInstance?.creator?.display}</g:link></dd>
					</g:if>

					<g:if test="${taskInstance?.createDate}">
						<dt><g:message code="task.createDate.label" /></dt>
						<dd><g:formatDate date="${taskInstance?.createDate}" /></dd>
					</g:if>

					<g:if test="${taskInstance?.endDate}">
						<dt><g:message code="task.endDate.label" /></dt>
						<dd><g:formatDate date="${taskInstance?.endDate}" /></dd>
					</g:if>
				
					<g:if test="${taskInstance?.responsibleUser}">
						<dt><g:message code="task.responsibleUser.label" /></dt>
						<dd><g:link controller="user" action="show" id="${taskInstance?.responsibleUser?.id}">${taskInstance?.responsibleUser?.display}</g:link></dd>
					</g:if>
				
					<g:if test="${taskInstance?.responsibleOrg}">
						<dt><g:message code="task.responsibleOrg.label" /></dt>
						<dd><g:link controller="organisation" action="show" id="${taskInstance?.responsibleOrg?.id}">${taskInstance?.responsibleOrg}</g:link></dd>
					</g:if>
				</dl>

				<g:form>
					<g:hiddenField id="task_id_${taskInstance?.id}" name="id" value="${taskInstance?.id}" />
					<div class="ui form-actions">
						<g:link action="edit" id="${taskInstance?.id}"
								class="ui icon button blue la-modern-button"
								role="button"
								aria-label="${message(code: 'ariaLabel.edit.universal')}">
							<i aria-hidden="true" class="write icon"></i>
						</g:link>
						<button class="ui negative button" type="submit" name="_action_delete">
							<i class="trash alternate outline icon"></i>
							<g:message code="default.button.delete.label" />
						</button>
					</div>
				</g:form>
	</body>
</html>
