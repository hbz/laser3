<%@ page import="de.laser.storage.RDStore; de.laser.Address" %>

<g:set var="entityName" value="${message(code: 'address.label')}" />
<laser:htmlStart text="${message(code:"default.edit.label", args:[entityName])}" />

	<semui:breadcrumbs>
		<g:if test="${addressInstance.org && (RDStore.OT_PROVIDER.id in addressInstance.org.getAllOrgTypeIds())}">
			<semui:crumb message="menu.public.all_providers" controller="organisation" action="listProvider"/>
			<semui:crumb message="${addressInstance.org.getDesignation()}" controller="organisation" action="show" id="${addressInstance.org.id}"/>
		</g:if>
		<g:else>
			<semui:crumb message="menu.public.all_orgs" controller="organisation" action="index"/>
		</g:else>
		<semui:crumb text="${g.message(code:'default.edit.label', args:[entityName])}" class="active"/>
	</semui:breadcrumbs>

		<semui:h1HeaderWithIcon message="default.edit.label" args="[entityName]" />

		<semui:messages data="${flash}" />

		<semui:errors bean="${addressInstance}" />

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
