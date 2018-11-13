<%@ page import="com.k_int.kbplus.Address" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'address.label', default: 'Address')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>

	<body>
	<semui:breadcrumbs>
		<g:if test="${(com.k_int.kbplus.RefdataValue.getByValueAndCategory('Provider', 'OrgRoleType')?.id in addressInstance?.org?.getallOrgRoleTypeIds())}">
			<semui:crumb message="menu.institutions.all_provider" controller="organisations" action="listProvider"/>
			<semui:crumb message="${addressInstance?.org?.getDesignation()}" controller="organisations" action="show" id="${addressInstance?.org?.id}"/>
			<semui:crumb text="${g.message(code:'default.edit.label', args:[entityName])}" class="active"/>
		</g:if>
		<g:else>
			<semui:crumb message="menu.institutions.all_orgs" controller="organisations" action="index"/>
			<semui:crumb message="${addressInstance?.org?.getDesignation()}" controller="organisations" action="show" id="${addressInstance?.org?.id}"/>
			<semui:crumb text="${g.message(code:'default.edit.label', args:[entityName])}" class="active"/>
		</g:else>
	</semui:breadcrumbs>

		<h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="default.edit.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<semui:errors bean="${addressInstance}" />

		<div class="ui grid">
			
			<div class="twelve wide column">

				<fieldset>
					<g:form class="ui form" action="edit" id="${addressInstance?.id}" >
						<g:hiddenField name="version" value="${addressInstance?.version}" />
						<fieldset>
							<% // <f:all bean="addressInstance"/> %>
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

            <aside class="four wide column">
            </aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
