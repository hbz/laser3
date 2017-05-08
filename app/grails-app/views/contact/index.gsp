
<%@ page import="com.k_int.kbplus.Contact" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'contact.label', default: 'Contact')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-contact" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-contact" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="contentType" title="${message(code: 'contact.contentType.label', default: 'ContentType')}" />
						
						<g:sortableColumn property="content" title="${message(code: 'contact.content.label', default: 'Content')}" />
						
						<th><g:message code="contact.type.label" default="Type" /></th>
					
						<th><g:message code="contact.prs.label" default="Prs" /></th>
					
						<th><g:message code="contact.org.label" default="Org" /></th>
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${contactInstanceList}" status="i" var="contactInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
										
						<td>${fieldValue(bean: contactInstance, field: "contentType")}</td>
					
						<td>${fieldValue(bean: contactInstance, field: "content")}</td>
						
						<td>${fieldValue(bean: contactInstance, field: "type")}</td>
					
						<td>${fieldValue(bean: contactInstance, field: "prs")}</td>
					
						<td>${fieldValue(bean: contactInstance, field: "org")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${contactInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
