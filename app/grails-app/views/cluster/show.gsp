
<%@ page import="com.k_int.kbplus.Cluster" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<g:set var="entityName" value="${message(code: 'cluster.label', default: 'Cluster')}" />
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
				
					<g:if test="${clusterInstance?.definition}">
						<dt><g:message code="cluster.definition.label" default="Definition" /></dt>
						
							<dd><g:fieldValue bean="${clusterInstance}" field="definition"/></dd>
						
					</g:if>
				
					<g:if test="${clusterInstance?.name}">
						<dt><g:message code="cluster.name.label" default="Name" /></dt>
						
							<dd><g:fieldValue bean="${clusterInstance}" field="name"/></dd>
						
					</g:if>
				
					<g:if test="${clusterInstance?.orgs}">
						<dt><g:message code="cluster.orgs.label" default="Orgs" /></dt>
						
							<g:each in="${clusterInstance.orgs}" var="o">
							<dd>${o?.roleType?.value} - <g:link controller="org" action="show" id="${o?.org?.id}">${o?.org?.name}</g:link></dd>
							</g:each>
						
					</g:if>
				
					<g:if test="${clusterInstance?.type}">
						<dt><g:message code="cluster.type.label" default="Type" /></dt>
						
							<dd>${clusterInstance?.type?.encodeAsHTML()}</dd>
						
					</g:if>
				
				</dl>

				<g:form>
					<g:hiddenField name="id" value="${clusterInstance?.id}" />
					<div class="form-actions">
						<g:link class="btn" action="edit" id="${clusterInstance?.id}">
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
