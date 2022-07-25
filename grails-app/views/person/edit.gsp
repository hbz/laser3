<%@ page import="de.laser.Person; de.laser.properties.PropertyDefinition" %>

<g:set var="entityName" value="${message(code: 'person.label')}" />
<laser:htmlStart text="${message(code:"default.edit.label", args:[entityName])}" />

		<ui:h1HeaderWithIcon message="default.edit.label" args="[entityName]" />

		<ui:messages data="${flash}" />

		<ui:errors bean="${personInstance}" />

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
<laser:htmlEnd />
