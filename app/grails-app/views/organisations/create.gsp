<%@ page import="com.k_int.kbplus.Org" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
		<title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
	    <semui:breadcrumbs>
            <semui:crumb message="menu.institutions.all_orgs" controller="organisations" action="index" />
            <semui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
	    </semui:breadcrumbs>

		<h1 class="ui header"><semui:headerIcon /><g:message code="default.create.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<semui:errors bean="${orgInstance}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<fieldset>
					<g:form class="ui form" action="create" >
						<fieldset>
                            <% // <f:all bean="addressInstance"/> %>
                            <g:render template="form"/>

							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.create.label" default="Create" />
								</button>
							</div>
						</fieldset>
					</g:form>
				</fieldset>

			</div><!-- .twelve -->

				<aside class="four wide column">
				</aside><!-- .four -->

			</div><!-- .grid -->

	</body>
</html>
