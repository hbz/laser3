
<%@ page import="com.k_int.kbplus.Numbers" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'numbers.label', default: 'Numbers')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-numbers" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-numbers" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<th><g:message code="numbers.type.label" default="Type" /></th>
					
						<g:sortableColumn property="number" title="${message(code: 'numbers.number.label', default: 'Number')}" />
					
						<g:sortableColumn property="startDate" title="${message(code: 'numbers.startDate.label', default: 'Start Date')}" />
					
						<g:sortableColumn property="endDate" title="${message(code: 'numbers.endDate.label', default: 'End Date')}" />
					
						<g:sortableColumn property="lastUpdated" title="${message(code: 'numbers.lastUpdated.label', default: 'Last Updated')}" />
					
						<g:sortableColumn property="dateCreated" title="${message(code: 'numbers.dateCreated.label', default: 'Date Created')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${numbersInstanceList}" status="i" var="numbersInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${numbersInstance.id}">${fieldValue(bean: numbersInstance, field: "type")}</g:link></td>
					
						<td>${fieldValue(bean: numbersInstance, field: "number")}</td>
					
						<td><g:formatDate date="${numbersInstance.startDate}" /></td>
					
						<td><g:formatDate date="${numbersInstance.endDate}" /></td>
					
						<td><g:formatDate date="${numbersInstance.lastUpdated}" /></td>
					
						<td><g:formatDate date="${numbersInstance.dateCreated}" /></td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${numbersInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
