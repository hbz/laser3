
<%@ page import="com.k_int.kbplus.Org" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-org" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-org" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="impId" title="${message(code: 'org.impId.label', default: 'Imp Id')}" />
					
						<g:sortableColumn property="comment" title="${message(code: 'org.comment.label', default: 'Comment')}" />
					
						<g:sortableColumn property="ipRange" title="${message(code: 'org.ipRange.label', default: 'Ip Range')}" />
					
						<g:sortableColumn property="sector" title="${message(code: 'org.sector.label', default: 'Sector')}" />
					
						<g:sortableColumn property="shortcode" title="${message(code: 'org.shortcode.label', default: 'Shortcode')}" />
					
						<g:sortableColumn property="scope" title="${message(code: 'org.scope.label', default: 'Scope')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${orgInstanceList}" status="i" var="orgInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${orgInstance.id}">${fieldValue(bean: orgInstance, field: "impId")}</g:link></td>
					
						<td>${fieldValue(bean: orgInstance, field: "comment")}</td>
					
						<td>${fieldValue(bean: orgInstance, field: "ipRange")}</td>
					
						<td>${fieldValue(bean: orgInstance, field: "sector")}</td>
					
						<td>${fieldValue(bean: orgInstance, field: "shortcode")}</td>
					
						<td>${fieldValue(bean: orgInstance, field: "scope")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${orgInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
