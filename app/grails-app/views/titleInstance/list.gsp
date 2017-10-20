
<%@ page import="com.k_int.kbplus.TitleInstance" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<g:set var="entityName" value="${message(code: 'titleInstance.label', default: 'TitleInstance')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="container">
				
				<div class="page-header">
					<h1><g:message code="default.list.label" args="[entityName]" /></h1>
				</div>

				<g:if test="${flash.message}">
				<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>
				
				<table class="table table-bordered table-striped">
					<thead>
						<tr>
						
							<g:sortableColumn property="impId" title="${message(code: 'titleInstance.impId.label', default: 'Imp Id')}" />
						
							<g:sortableColumn property="title" title="${message(code: 'titleInstance.title.label', default: 'Title')}" />
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${titleInstanceInstanceList}" var="titleInstanceInstance">
						<tr>
						
							<td>${fieldValue(bean: titleInstanceInstance, field: "impId")}</td>
						
							<td>${fieldValue(bean: titleInstanceInstance, field: "title")}</td>
						
							<td class="link">
								<g:link action="show" id="${titleInstanceInstance.id}" class="btn btn-small">Show &raquo;</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${titleInstanceInstanceTotal}" />
				</div>

		</div>
	</body>
</html>
