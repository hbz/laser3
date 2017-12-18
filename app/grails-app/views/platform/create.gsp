<%@ page import="com.k_int.kbplus.Platform" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>

			<h1 class="ui header"><g:message code="default.create.label" args="[entityName]" /></h1>

			<semui:messages data="${flash}" />

			<semui:errors bean="${platformInstance}" />

			<fieldset>
				<g:form class="ui form" action="create" >
					<fieldset>
						<f:all bean="platformInstance"/>
						<div class="ui segment form-actions">
							<button type="submit" class="ui button">
								<i class="icon-ok icon-white"></i>
								<g:message code="default.button.create.label" default="Create" />
							</button>
						</div>
					</fieldset>
				</g:form>
			</fieldset>

	</body>
</html>
