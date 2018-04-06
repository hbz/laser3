
<%@ page import="com.k_int.kbplus.CreatorTitle" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'creatorTitle.label', default: 'CreatorTitle')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><semui:headerIcon /><g:message code="default.list.label" args="[entityName]" /></h1>

        <semui:messages data="${flash}" />

		<div class="ui grid">

			<div class="twelve wide column">
				
				<table class="ui celled striped table">
					<thead>
						<tr>
						
							<th class="header"><g:message code="creatorTitle.creator.label" default="Creator" /></th>
						
							<g:sortableColumn property="dateCreated" title="${message(code: 'creatorTitle.dateCreated.label', default: 'Date Created')}" />
						
							<g:sortableColumn property="lastUpdated" title="${message(code: 'creatorTitle.lastUpdated.label', default: 'Last Updated')}" />
						
							<th class="header"><g:message code="creatorTitle.role.label" default="Role" /></th>
						
							<th class="header"><g:message code="creatorTitle.title.label" default="Title" /></th>
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${creatorTitleInstanceList}" var="creatorTitleInstance">
						<tr>
						
							<td>${fieldValue(bean: creatorTitleInstance, field: "creator")}</td>
						
							<td><g:formatDate date="${creatorTitleInstance.dateCreated}" /></td>
						
							<td><g:formatDate date="${creatorTitleInstance.lastUpdated}" /></td>
						
							<td>${fieldValue(bean: creatorTitleInstance, field: "role")}</td>
						
							<td>${fieldValue(bean: creatorTitleInstance, field: "title")}</td>
						