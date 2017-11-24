
<%@ page import="com.k_int.kbplus.Contact" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'contact.label', default: 'Contact')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><g:message code="default.show.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />


	<div class="ui grid">

			<div class="twelve wide column">

				
				<div class="inline-lists">
				<dl>			
					<g:if test="${contactInstance?.contentType}">
						<dt><g:message code="contact.contentType.label" default="ContentType" /></dt>
						
							<dd><g:fieldValue bean="${contactInstance}" field="contentType"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${contactInstance?.content}">
						<dt><g:message code="contact.content.label" default="Content" /></dt>
						
							<dd><g:fieldValue bean="${contactInstance}" field="content"/></dd>
						
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
				
				<dl class="debug-only">
					<g:if test="${contactInstance?.prs?.tenant}">
						<dt><g:message code="person.tenant.label" default="Tenant (derived from Prs)" /></dt>
						<dd><g:link controller="org" action="show" id="${contactInstance?.prs?.tenant?.id}">${contactInstance?.prs?.tenant?.encodeAsHTML()}</g:link></dd>
					</g:if>
				</dl>
				<dl class="debug-only">
					<g:if test="${contactInstance?.prs?.isPublic}">
						<dt><g:message code="person.isPublic.label" default="IsPublic (derived from Prs)" /></dt>
						<dd><g:link controller="org" action="show" id="${contactInstance?.prs?.isPublic?.id}">${contactInstance?.prs?.isPublic?.encodeAsHTML()}</g:link></dd>
					</g:if>
				</dl>
				</div>
				<g:form>
					<g:hiddenField name="id" value="${contactInstance?.id}" />
					<div class="ui segment form-actions">
						<g:link class="ui button" action="edit" id="${contactInstance?.id}">
							<i class="icon-pencil"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="ui negative button" type="submit" name="_action_delete">
							<i class="icon-trash icon-white"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
				</g:form>

			</div><!-- .twelve -->

            <div class="four wide column">
                <g:render template="../templates/sideMenu" />
            </div><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
