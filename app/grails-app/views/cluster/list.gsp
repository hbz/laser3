
<%@ page import="com.k_int.kbplus.Cluster" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<g:set var="entityName" value="${message(code: 'cluster.label', default: 'Cluster')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="container">
		<div class="row-fluid">
			
			<div class="span3">
				<div class="well">
					<ul class="nav nav-list">
						<li class="nav-header">${entityName}</li>
						<li class="active">
							<g:link class="list" action="list">
								<i class="icon-list icon-white"></i>
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
					<h1><g:message code="default.list.label" args="[entityName]" /></h1>
				</div>

				<g:if test="${flash.message}">
				<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>
				
				<table class="table table-bordered table-striped">
					<thead>
						<tr>
						
							<g:sortableColumn property="definition" title="${message(code: 'cluster.definition.label', default: 'Definition')}" />
						
							<g:sortableColumn property="name" title="${message(code: 'cluster.name.label', default: 'Name')}" />
						
							<th class="header"><g:message code="cluster.type.label" default="Type" /></th>
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${clusterInstanceList}" var="clusterInstance">
						<tr>
						
							<td>${fieldValue(bean: clusterInstance, field: "definition")}</td>
						
							<td>${fieldValue(bean: clusterInstance, field: "name")}</td>
						
							<td>${fieldValue(bean: clusterInstance, field: "type")}</td>
						
							<td class="link">
								<g:link action="show" id="${clusterInstance.id}" class="btn btn-small">Show &raquo;</g:link>
								<g:link action="edit" id="${clusterInstance.id}" class="btn btn-small">Edit</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${clusterInstanceTotal}" />
				</div>
			</div>

		</div>
		</div>
	</body>
</html>
