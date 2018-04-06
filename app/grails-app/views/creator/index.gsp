
<%@ page import="com.k_int.kbplus.Creator" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'creator.label', default: 'Creator')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-creator" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-creator" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="firstname" title="${message(code: 'creator.firstname.label', default: 'Firstname')}" />
					
						<g:sortableColumn property="middlename" title="${message(code: 'creator.middlename.label', default: 'Middlename')}" />
					
						<th><g:message code="creator.gnd_id.label" default="Gndid" /></th>
					
						<g:sortableColumn property="globalUID" title="${message(code: 'creator.globalUID.label', default: 'Global UID')}" />
					
						<g:sortableColumn property="dateCreated" title="${message(code: 'creator.dateCreated.label', default: 'Date Created')}" />
					
						<g:sortableColumn property="lastUpdated" title="${message(code: 'creator.lastUpdated.label', default: 'Last Updated')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${creatorInstanceList}" status="i" var="creatorInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${creatorInstance.id}">${fieldValue(bean: creatorInstance, field: "firstname")}</g:link></td>
					
						<td>${fieldValue(bean: creatorInstance, field: "middlename")}</td>
					
						<td>${fieldValue(bean: creatorInstance, field: "gnd_id")}</td>
					
						<td>${fieldValue(bean: creatorInstance, field: "globalUID")}</td>
					
						<td><g:formatDate date="${creatorInstance.dateCreated}" /></td>
					
						<td><g:formatDate date="${creatorInstance.lastUpdated}" /></td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${creatorInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
