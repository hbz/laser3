
<%@ page import="com.k_int.kbplus.Numbers" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'numbers.label', default: 'Numbers')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>

        <semui:messages data="${flash}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<dl>
				
					<g:if test="${numbersInstance?.type}">
						<dt><g:message code="numbers.type.label" default="Type" /></dt>
						
							<dd><g:link controller="refdataValue" action="show" id="${numbersInstance?.type?.id}">${numbersInstance?.type?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
					<g:if test="${numbersInstance?.number}">
						<dt><g:message code="numbers.number.label" default="Number" /></dt>
						
							<dd><g:fieldValue bean="${numbersInstance}" field="number"/></dd>
						
					</g:if>
				
					<g:if test="${numbersInstance?.startDate}">
						<dt><g:message code="numbers.startDate.label" default="Start Date" /></dt>
						
							<dd><g:formatDate date="${numbersInstance?.startDate}" /></dd>
						
					</g:if>
				
					<g:if test="${numbersInstance?.endDate}">
						<dt><g:message code="numbers.endDate.label" default="End Date" /></dt>
						
							<dd><g:formatDate date="${numbersInstance?.endDate}" /></dd>
						
					</g:if>
				
					<g:if test="${numbersInstance?.lastUpdated}">
						<dt><g:message code="numbers.lastUpdated.label" default="Last Updated" /></dt>
						
							<dd><g:formatDate date="${numbersInstance?.lastUpdated}" /></dd>
						
					</g:if>
				
					<g:if test="${numbersInstance?.dateCreated}">
						<dt><g:message code="numbers.dateCreated.label" default="Date Created" /></dt>
						
							<dd><g:formatDate date="${numbersInstance?.dateCreated}" /></dd>
						
					</g:if>
				
					<g:if test="${numbersInstance?.org}">
						<dt><g:message code="numbers.org.label" default="Org" /></dt>
						
							<dd><g:link controller="org" action="show" id="${numbersInstance?.org?.id}">${numbersInstance?.org?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
				</dl>

				<g:form class="ui form">
					<g:hiddenField name="id" value="${numbersInstance?.id}" />
					<div class="ui form-actions">
						<g:link class="ui button" action="edit" id="${numbersInstance?.id}">
							<i class="write icon"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="ui button negative" type="submit" name="_action_delete">
							<i class="trash icon"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
				</g:form>

			</div><!-- .twelve -->

            <aside class="four wide column">
                <g:render template="../templates/sideMenu" />
            </aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
