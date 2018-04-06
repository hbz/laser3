
<%@ page import="com.k_int.kbplus.CreatorTitle" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'creatorTitle.label', default: 'CreatorTitle')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>

        <semui:messages data="${flash}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<dl>
				
					<g:if test="${creatorTitleInstance?.creator}">
						<dt><g:message code="creatorTitle.creator.label" default="Creator" /></dt>
						
							<dd><g:link controller="creator" action="show" id="${creatorTitleInstance?.creator?.id}">${creatorTitleInstance?.creator?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
					<g:if test="${creatorTitleInstance?.dateCreated}">
						<dt><g:message code="creatorTitle.dateCreated.label" default="Date Created" /></dt>
						
							<dd><g:formatDate date="${creatorTitleInstance?.dateCreated}" /></dd>
						
					</g:if>
				
					<g:if test="${creatorTitleInstance?.lastUpdated}">
						<dt><g:message code="creatorTitle.lastUpdated.label" default="Last Updated" /></dt>
						
							<dd><g:formatDate date="${creatorTitleInstance?.lastUpdated}" /></dd>
						
					</g:if>
				
					<g:if test="${creatorTitleInstance?.role}">
						<dt><g:message code="creatorTitle.role.label" default="Role" /></dt>
						
							<dd><g:link controller="refdataValue" action="show" id="${creatorTitleInstance?.role?.id}">${creatorTitleInstance?.role?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
					<g:if test="${creatorTitleInstance?.title}">
						<dt><g:message code="creatorTitle.title.label" default="Title" /></dt>
						
							<dd><g:link controller="titleInstance" action="show" id="${creatorTitleInstance?.title?.id}">${creatorTitleInstance?.title?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
				</dl>

				<g:form class="ui form">
					<g:hiddenField name="id" value="${creatorTitleInstance?.id}" />
					<div class="ui form-actions">
						<g:link class="ui button" action="edit" id="${creatorTitleInstance?.id}">
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
