<%@ page import="com.k_int.kbplus.Person; com.k_int.properties.PropertyDefinition" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<div>
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
						<li>
							<g:link class="create" action="create">
								<i class="icon-plus"></i>
								<g:message code="default.create.label" args="[entityName]" />
							</g:link>
						</li>
					</ul>
				</div>
			</div>
			
			<div class="span9">

					<h1 class="ui header"><g:message code="default.edit.label" args="[entityName]" /></h1>

				<semui:messages data="${flash}" />

				<g:hasErrors bean="${personInstance}">
				<bootstrap:alert class="alert-error">
				<ul>
					<g:eachError bean="${personInstance}" var="error">
					<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
				</g:hasErrors>

				<fieldset>
					<g:form class="ui form" action="edit" id="${personInstance?.id}" >
						<g:hiddenField name="version" value="${personInstance?.version}" />
						<fieldset>
							<% // <f:all bean="personInstance"/> %>
							<g:render template="form"/>
							
							<div class="ui segment form-actions">
								<button type="submit" class="ui primary button">
									<i class="icon-ok icon-white"></i>
									<g:message code="default.button.update.label" default="Update" />
								</button>
								<button type="submit" class="ui negative button" name="_action_delete" formnovalidate>
									<i class="icon-trash icon-white"></i>
									<g:message code="default.button.delete.label" default="Delete" />
								</button>
							</div>
						</fieldset>
					</g:form>
				</fieldset>

			</div>

		</div>
		</div>
	</body>
</html>
