
<%@ page import="com.k_int.kbplus.TitleInstance" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'titleInstance.label', default: 'TitleInstance')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div>

					<h1 class="ui header"><g:message code="default.list.label" args="[entityName]" /></h1>

			<semui:messages data="${flash}" />
				
				<table class="ui celled striped table">
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
								<g:link action="show" id="${titleInstanceInstance.id}" class="ui tiny button">Show</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>

					<semui:paginate total="${titleInstanceInstanceTotal}" />


		</div>
	</body>
</html>
