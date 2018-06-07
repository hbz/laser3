
<%@ page import="com.k_int.kbplus.Numbers" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'numbers.label', default: 'Numbers')}" />
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
						
							<th class="header"><g:message code="numbers.type.label" default="Type" /></th>
						
							<g:sortableColumn property="number" title="${message(code: 'numbers.number.label', default: 'Number')}" />
						
							<g:sortableColumn property="startDate" title="${message(code: 'numbers.startDate.label', default: 'Start Date')}" />
						
							<g:sortableColumn property="endDate" title="${message(code: 'numbers.endDate.label', default: 'End Date')}" />
						
							<g:sortableColumn property="lastUpdated" title="${message(code: 'numbers.lastUpdated.label', default: 'Last Updated')}" />
						
							<g:sortableColumn property="dateCreated" title="${message(code: 'numbers.dateCreated.label', default: 'Date Created')}" />
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${numbersInstanceList}" var="numbersInstance">
						<tr>
						
							<td>${fieldValue(bean: numbersInstance, field: "type")}</td>
						
							<td>${fieldValue(bean: numbersInstance, field: "number")}</td>
						
							<td><g:formatDate date="${numbersInstance.startDate}" /></td>
						
							<td><g:formatDate date="${numbersInstance.endDate}" /></td>
						
							<td><g:formatDate date="${numbersInstance.lastUpdated}" /></td>
						
							<td><g:formatDate date="${numbersInstance.dateCreated}" /></td>
						