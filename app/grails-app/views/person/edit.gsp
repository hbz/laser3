<%@ page import="com.k_int.kbplus.Person; com.k_int.properties.PropertyDefinition" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>

		<h1 class="ui header"><g:message code="default.edit.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<semui:errors bean="${personInstance}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<fieldset>
					<g:form class="ui form" action="edit" id="${personInstance?.id}" >
						<g:hiddenField name="version" value="${personInstance?.version}" />
						<fieldset>
							<% // <f:all bean="personInstance"/> %>
							<g:render template="form"/>
							
							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.update.label" default="Update" />
								</button>
								<button type="submit" class="ui negative button" name="_action_delete" formnovalidate>
									<i class="trash alternate icon"></i>
									<g:message code="default.button.delete.label" default="Delete" />
								</button>
							</div>
						</fieldset>
					</g:form>
				</fieldset>

			</div><!-- .twelve -->

			<div class="four wide column">
				<semui:card text="${entityName}">
					<ul class="nav nav-list">
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
				</semui:card>
			</div><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
