<%@ page import="com.k_int.kbplus.Org" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
		<h2>DEPRECATED</h2>
		<div class="row-fluid">
			
			<div class="span3">
				<div class="well">
					<ul class="nav nav-list">
						<li class="nav-header">${entityName}</li>
						<li>
							<g:link class="list" action="list">
								<i class="icon-list"></i>
								<g:message code="default.list.label" args="[entityName]" />
							</g:link>
						</li>
						<li class="active">
							<g:link class="create" action="create">
								<i class="icon-plus icon-white"></i>
								<g:message code="default.create.label" args="[entityName]" />
							</g:link>
						</li>
					</ul>
				</div>
			</div>
			
			<div class="span9">

				<h1 class="ui header"><g:message code="default.create.label" args="[entityName]" /></h1>


				<semui:messages data="${flash}" />

				<g:hasErrors bean="${orgInstance}">
				<bootstrap:alert class="alert-error">
				<ul>
					<g:eachError bean="${orgInstance}" var="error">
					<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
				</g:hasErrors>

				<fieldset>
					<g:form class="ui form" action="create">
						<fieldset>
							<% // <f:all bean="orgInstance"/> %>
							<g:render template="form"/>
							
							<div class="ui segment form-actions">
								<button id="SubmitButton" type="submit" class="ui primary button">
									<i class="icon-ok icon-white"></i>
									<g:message code="default.button.create.label" default="Create" />
								</button>
							</div>
						</fieldset>
					</g:form>
				</fieldset>
				
			</div>

		</div>
	</body>
</html>
