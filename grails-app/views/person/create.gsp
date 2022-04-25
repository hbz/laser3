<%@ page import="de.laser.Person" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'person.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>

		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.create.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<semui:errors bean="${personInstance}" />

		<div class="ui grid">

			<div class="twelve wide column">

					<g:form class="ui form" action="create" >
						<fieldset>
							<laser:render template="form"/>
							
							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.create.label"/>
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
