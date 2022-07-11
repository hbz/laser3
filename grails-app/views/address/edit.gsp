<%@ page import="de.laser.storage.RDStore; de.laser.Address" %>

<g:set var="entityName" value="${message(code: 'address.label')}" />
<laser:htmlStart text="${message(code:"default.edit.label", args:[entityName])}" />

	<ui:breadcrumbs>
		<g:if test="${addressInstance.org && (RDStore.OT_PROVIDER.id in addressInstance.org.getAllOrgTypeIds())}">
			<ui:crumb message="menu.public.all_providers" controller="organisation" action="listProvider"/>
			<ui:crumb message="${addressInstance.org.getDesignation()}" controller="organisation" action="show" id="${addressInstance.org.id}"/>
		</g:if>
		<g:else>
			<ui:crumb message="menu.public.all_orgs" controller="organisation" action="index"/>
		</g:else>
		<ui:crumb text="${g.message(code:'default.edit.label', args:[entityName])}" class="active"/>
	</ui:breadcrumbs>

		<ui:h1HeaderWithIcon message="default.edit.label" args="[entityName]" />

		<ui:messages data="${flash}" />

		<ui:errors bean="${addressInstance}" />

		<div class="ui grid">
			
			<div class="twelve wide column">

					<g:form class="ui form" action="edit" id="${addressInstance.id}" >
						<g:hiddenField name="version" value="${addressInstance.version}" />
						<fieldset>
							<laser:render template="form"/>
							
							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.update.label" />
								</button>
								<button type="submit" class="ui negative button la-modern-button" name="_action_delete" formnovalidate>
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
<laser:htmlEnd />
