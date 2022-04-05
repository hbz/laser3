
<%@ page import="de.laser.Doc" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'doc.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>

			<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.list.label" args="[entityName]" /></h1>

			<semui:messages data="${flash}" />
				
				<table class="ui sortable celled la-js-responsive-table la-table table">
					<thead>
						<tr>
						
							<th class="header"><g:message code="default.status.label" /></th>
						
							<th class="header"><g:message code="default.type.label" /></th>
						
							<g:sortableColumn property="content" title="${message(code: 'default.content.label')}" />
						
							<g:sortableColumn property="uuid" title="${message(code: 'doc.uuid.label')}" />
						
							<g:sortableColumn property="contentType" title="${message(code: 'doc.contentType.label')}" />
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${docInstanceList}" var="docInstance">
						<tr>
						
							<td>${fieldValue(bean: docInstance, field: "status")}</td>
						
							<td>${fieldValue(bean: docInstance, field: "type")}</td>
						
							<td>${fieldValue(bean: docInstance, field: "content")}</td>
						
							<td>${fieldValue(bean: docInstance, field: "uuid")}</td>
						
							<td>${fieldValue(bean: docInstance, field: "contentType")}</td>
						
							<td class="link">
								<g:link action="show" id="${docInstance.id}" class="ui tiny button">${message('code':'default.button.show.label')}</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>

			<semui:paginate total="${docInstanceTotal}" />

	</body>
</html>
