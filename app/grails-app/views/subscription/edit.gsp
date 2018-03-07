<%@ page import="com.k_int.kbplus.Subscription" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'subscription.label', default: 'Subscription')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><g:message code="default.edit.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />
			<semui:errors bean="${subscriptionInstance}" />

		<div class="ui grid">
			
			<div class="twelve wide column">

				<fieldset>
					<g:form class="ui form" action="edit" id="${subscriptionInstance?.id}" >
						<g:hiddenField name="version" value="${subscriptionInstance?.version}" />
						<fieldset>
							<f:all bean="subscriptionInstance"/>
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

			<aside class="four wide column">
				<g:render template="../templates/sideMenu" />
			</aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
