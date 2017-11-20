<%@ page import="com.k_int.kbplus.Platform" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>

		<h1 class="ui header"><g:message code="default.edit.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<g:hasErrors bean="${platformInstance}">
			<bootstrap:alert class="alert-error">
				<ul>
					<g:eachError bean="${platformInstance}" var="error">
						<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
			</bootstrap:alert>
		</g:hasErrors>

		<div class="ui grid">

			<div class="twelve wide column">

				<fieldset>
					<g:form class="form-horizontal" action="edit" id="${platformInstance?.id}" >
						<g:hiddenField name="version" value="${platformInstance?.version}" />
						<fieldset>
							<f:all bean="platformInstance"/>
							<div class="form-actions">
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

			</div><!-- .twelve -->

			<div class="four wide column">
				<g:render template="../templates/sideMenu" />
			</div><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
