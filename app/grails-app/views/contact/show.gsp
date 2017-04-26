
<%@ page import="com.k_int.kbplus.Contact" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<g:set var="entityName" value="${message(code: 'contact.label', default: 'Contact')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="row-fluid">
			
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

				<div class="page-header">
					<h1><g:message code="default.show.label" args="[entityName]" /></h1>
				</div>

				<g:if test="${flash.message}">
				<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>

				
<div class="inline-lists">
				<dl>			
					<g:if test="${contactInstance?.mail}">
						<dt><g:message code="contact.mail.label" default="Mail" /></dt>
						
							<dd><g:fieldValue bean="${contactInstance}" field="mail"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${contactInstance?.phone}">
						<dt><g:message code="contact.phone.label" default="Phone" /></dt>
						
							<dd><g:fieldValue bean="${contactInstance}" field="phone"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${contactInstance?.type}">
						<dt><g:message code="contact.type.label" default="Type" /></dt>
						
							<dd><g:link controller="refdataValue" action="show" id="${contactInstance?.type?.id}">${contactInstance?.type?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${contactInstance?.prs}">
						<dt><g:message code="contact.prs.label" default="Prs" /></dt>
						
							<dd><g:link controller="person" action="show" id="${contactInstance?.prs?.id}">${contactInstance?.prs?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${contactInstance?.org}">
						<dt><g:message code="contact.org.label" default="Org" /></dt>
						
							<dd><g:link controller="org" action="show" id="${contactInstance?.org?.id}">${contactInstance?.org?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
				</dl>
				
				<dl>
					<g:if test="${contactInstance?.prs?.owner}">
						<dt><g:message code="person.owner.label" default="Owner (derived from Prs)" /></dt>
						<dd><g:link controller="org" action="show" id="${contactInstance?.prs?.owner?.id}">${contactInstance?.prs?.owner?.encodeAsHTML()}</g:link></dd>
					</g:if>
				</dl>
				<dl>
					<g:if test="${contactInstance?.prs?.isPublic}">
						<dt><g:message code="person.isPublic.label" default="IsPublic (derived from Prs)" /></dt>
						<dd><g:link controller="org" action="show" id="${contactInstance?.prs?.isPublic?.id}">${contactInstance?.prs?.isPublic?.encodeAsHTML()}</g:link></dd>
					</g:if>
				</dl>
</div>
				<g:form>
					<g:hiddenField name="id" value="${contactInstance?.id}" />
					<div class="form-actions">
						<g:link class="btn" action="edit" id="${contactInstance?.id}">
							<i class="icon-pencil"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="btn btn-danger" type="submit" name="_action_delete">
							<i class="icon-trash icon-white"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
				</g:form>

			</div>

		</div>
	</body>
</html>
