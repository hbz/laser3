<%@ page import="com.k_int.kbplus.Person" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>

		<h1 class="ui header"><g:message code="default.create.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<semui:errors bean="${personInstance}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<fieldset>
					<g:form class="ui form" action="create" >
						<fieldset>
                            <% // <f:all bean="personInstance" /> %>
							<g:render template="form"/>
							
							<div class="ui segment form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.create.label" default="Create" />
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
