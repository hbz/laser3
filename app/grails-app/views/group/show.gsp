
<%@ page import="com.k_int.kbplus.Group" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<g:set var="entityName" value="${message(code: 'group.label', default: 'Group')}" />
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

				<dl>
				
					<g:if test="${groupInstance?.owner}">
						<dt><g:message code="group.owner.label" default="Owner" /></dt>
						
							<dd><g:link controller="org" action="show" id="${groupInstance?.owner?.id}">${groupInstance?.owner?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
					<g:if test="${groupInstance?.definition}">
						<dt><g:message code="group.definition.label" default="Definition" /></dt>
						
							<dd><g:fieldValue bean="${groupInstance}" field="definition"/></dd>
						
					</g:if>
				
					<g:if test="${groupInstance?.name}">
						<dt><g:message code="group.name.label" default="Name" /></dt>
						
							<dd><g:fieldValue bean="${groupInstance}" field="name"/></dd>
						
					</g:if>
				
					<g:if test="${groupInstance?.type}">
						<dt><g:message code="group.type.label" default="Type" /></dt>
						
							<dd><g:link controller="refdataValue" action="show" id="${groupInstance?.type?.id}">${groupInstance?.type?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
				</dl>

				<g:form>
					<g:hiddenField name="id" value="${groupInstance?.id}" />
					<div class="form-actions">
						<g:link class="btn" action="edit" id="${groupInstance?.id}">
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
