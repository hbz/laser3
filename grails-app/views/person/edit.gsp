<%@ page import="de.laser.Person; de.laser.properties.PropertyDefinition" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'person.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>

		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.edit.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<semui:errors bean="${personInstance}" />

		<div class="ui grid">

			<div class="twelve wide column">

					<g:form class="ui form" action="edit" id="${personInstance?.id}" >
						<g:hiddenField name="version" value="${personInstance?.version}" />
						<fieldset>
							<laser:render template="form"/>
							
							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.update.label" />
								</button>
								<button type="submit" class="ui negative button" name="_action_delete" formnovalidate>
									<i class="trash alternate outline icon"></i>
									<g:message code="default.button.delete.label" />
								</button>
							</div>
						</fieldset>
					</g:form>

			</div><!-- .twelve -->

			<aside class="four wide column">
			</aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
