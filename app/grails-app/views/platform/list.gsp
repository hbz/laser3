
<%@ page import="com.k_int.kbplus.Platform" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>

			<h1 class="ui header"><g:message code="default.list.label" args="[entityName]" /></h1>

			<semui:messages data="${flash}" />

			<semui:filter>
				<g:form action="list" method="get" class="form-inline">
					<label>${message(code:'default.search.text', default:'Search text')} : </label> <input type="text" name="q" placeholder="${message(code:'default.search.ph', default:'enter search term...')}" value="${params.q?.encodeAsHTML()}"  /> &nbsp;
					<input type="submit" class="ui primary button" value="${message(code:'default.button.search.label', default:'Search')}" />
				</g:form><br/>
			</semui:filter>

			<table class="ui celled striped table">
				<thead>
					<tr>

						<g:sortableColumn property="name" title="${message(code: 'platform.name.label', default: 'Name')}" />

						<th></th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${platformInstanceList}" var="platformInstance">
					<tr>

						<td>${fieldValue(bean: platformInstance, field: "name")}</td>

						<td class="link">
							<g:link action="show" id="${platformInstance.id}" class="ui tiny button">${message(code:'default.button.show.label', default:'Show')}</g:link>
						</td>
					</tr>
				</g:each>
				</tbody>
			</table>

				<semui:paginate  action="list" controller="platform" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${platformInstanceTotal}" />


	</body>
</html>
