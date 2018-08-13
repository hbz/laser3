
<%@ page import="com.k_int.kbplus.IdentifierOccurrence" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div>
			
			<div class="span3">
				<div class="well">
					<ul class="nav nav-list">
						<li class="nav-header">${entityName}</li>
						<li>
							<g:link class="list" action="list">
								<i class="icon-list"></i>
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
			</div>
			
			<div class="span9">


				<h1 class="ui header"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>

				<semui:messages data="${flash}" />
				<dl>
				
					<g:if test="${identifierOccurrenceInstance?.org}">
						<dt><g:message code="identifierOccurrence.org.label" default="Org" /></dt>
						
							<dd><g:link controller="org" action="show" id="${identifierOccurrenceInstance?.org?.id}">${identifierOccurrenceInstance?.org}</g:link></dd>
						
					</g:if>
				
					<g:if test="${identifierOccurrenceInstance?.ti}">
						<dt><g:message code="identifierOccurrence.ti.label" default="Ti" /></dt>
						
							<dd><g:link controller="titleDetails" action="show" id="${identifierOccurrenceInstance?.ti?.id}">${identifierOccurrenceInstance?.ti}</g:link></dd>
						
					</g:if>
				
					<g:if test="${identifierOccurrenceInstance?.tipp}">
						<dt><g:message code="identifierOccurrence.tipp.label" default="Tipp" /></dt>
						
							<dd><g:link controller="titleInstancePackagePlatform" action="show" id="${identifierOccurrenceInstance?.tipp?.id}">${identifierOccurrenceInstance?.tipp}</g:link></dd>
						
					</g:if>
				
					<g:if test="${identifierOccurrenceInstance?.identifier}">
						<dt><g:message code="identifierOccurrence.identifier.label" default="Identifier" /></dt>
						
							<dd><g:link controller="identifier" action="show" id="${identifierOccurrenceInstance?.identifier?.id}">${identifierOccurrenceInstance?.identifier}</g:link></dd>
						
					</g:if>
				
				</dl>

				<g:form>
                                    <sec:ifAnyGranted roles="ROLE_ADMIN">
					<g:hiddenField name="id" value="${identifierOccurrenceInstance?.id}" />
					<div class="ui form-actions">
						<g:link class="ui button" action="edit" id="${identifierOccurrenceInstance?.id}">
							<i class="write icon"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="ui negative button" type="submit" name="_action_delete">
							<i class="trash alternate icon"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
                                    </sec:ifAnyGranted>
				</g:form>

			</div>

		</div>
	</body>
</html>
